package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.StockCheckResponse;
import com.example.demo.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.example.demo.model.StockCheckRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final Map<Long, Integer> dummyInventory = new HashMap<>();

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.order.minimum-charge}")
    private BigDecimal minimumCharge;

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    @Autowired
    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;

        dummyInventory.put(1L, 10);
        dummyInventory.put(2L, 5);
        dummyInventory.put(3L, 20);
    }

    @Autowired
    private OrderItemService orderItemService;

    public Order createOrder(Long customerId, Map<Long, Integer> dishQuantities) {
        if (dishQuantities == null || dishQuantities.isEmpty()) {
            throw new IllegalArgumentException("Dish quantities map must not be null or empty");
        }

        // Convert map to separate lists for backward compatibility with StockCheckRequest
        List<Long> dishIds = new ArrayList<>(dishQuantities.keySet());
        List<Integer> quantities = new ArrayList<>(dishQuantities.values());

        // 1. Build stock check request message
        StockCheckRequest stockCheckRequest = new StockCheckRequest();
        stockCheckRequest.setDishIds(dishIds);
        stockCheckRequest.setQuantities(quantities);

        // 2. Send message and wait for reply synchronously
        String exchange = "inventory.exchange";    // inventory exchange
        String routingKey = "inventory.stock.check"; // routing key

        // Send request and wait for response
        StockCheckResponse response;
        try {
            response = (StockCheckResponse) rabbitTemplate.convertSendAndReceive(
                exchange, 
                routingKey, 
                stockCheckRequest
            );
            
            if (response == null) {
                throw new RuntimeException("No response received from inventory service");
            }
            
            // 3. Check if stock is available
            if (!response.isStockAvailable()) {
                throw new IllegalArgumentException("Insufficient stock: " + response.getMessage());
            }
            
            // 4. Check minimum charge
            if (response.getTotalAmount() == null || response.getTotalAmount().compareTo(minimumCharge) < 0) {
                throw new IllegalArgumentException("Order total " + response.getTotalAmount() + 
                    " is below minimum charge of " + minimumCharge);
            }
            
            // 5. Get dish prices from response
            Map<Long, BigDecimal> dishPrices = response.getDishPrices();
            if (dishPrices == null || dishPrices.isEmpty()) {
                throw new RuntimeException("No dish price info returned from inventory");
            }
            
            // 6. Build order items
            List<OrderItem> orderItems = new ArrayList<>();
            for (int i = 0; i < dishIds.size(); i++) {
                Long dishId = dishIds.get(i);
                Integer quantity = quantities.get(i);

                BigDecimal price = dishPrices.get(dishId);
                if (price == null) {
                    throw new IllegalArgumentException("Price not found for dishId: " + dishId);
                }

                OrderItem item = new OrderItem();
                item.setDishId(dishId);
                item.setQuantity(quantity);
                item.setPricePerUnit(price);
                item.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
                orderItems.add(item);
            }

            // 7. Create and save order
            Order order = new Order();
            order.setCustomerId(customerId);
            order.setItems(orderItems);
            order.setTotalPrice(orderItems.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            order.setStatus("confirmed");
            order.setPaymentConfirmed(false);

            // 8. Process payment
            boolean paymentSuccess = processPayment(order);
            if (!paymentSuccess) {
                throw new RuntimeException("Payment processing failed");
            }

            // 9. Save order and order items
            Order savedOrder = orderRepository.save(order);
            for (OrderItem item : orderItems) {
                item.setOrder(savedOrder);
            }
            orderItemService.saveOrderItems(orderItems);

            return savedOrder;
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    // Dummy method example (replace with actual price fetching logic)
    private BigDecimal fetchPriceForDish(Long dishId) {
        // You can return dummy prices here or query your dishes repository
        if (dishId == 1L) return BigDecimal.valueOf(10);
        if (dishId == 2L) return BigDecimal.valueOf(20);
        if (dishId == 3L) return BigDecimal.valueOf(30);
        return BigDecimal.valueOf(5); // default fallback price
    }


    public BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getTotalPrice() == null ? BigDecimal.ZERO : item.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private boolean checkStockAvailability(Order order) {
        for (OrderItem item : order.getItems()) {
            Long dishId = item.getDishId();
            Integer quantityRequested = item.getQuantity();

            Integer available = dummyInventory.get(dishId);
            if (available == null || available < quantityRequested) {
                return false;
            }
        }
        return true;
    }

    private void deductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Long dishId = item.getDishId();
            Integer quantity = item.getQuantity();
            dummyInventory.put(dishId, dummyInventory.get(dishId) - quantity);
        }
    }


    private boolean processPayment(Order order) {
        Long customerId = order.getCustomerId();  // Use the customer ID from the order
        BigDecimal totalOrderPrice = order.getTotalPrice();

        try {
            // Get the customer's current balance for customer ID 5
            String balanceUrl = "http://localhost:8083/api/customers/" + customerId + "/balance";
            Float currentBalance = restTemplate.getForObject(balanceUrl, Float.class);

            if (currentBalance == null || currentBalance < totalOrderPrice.floatValue()) {
                return false;
            }

            // Deduct the payment from customer ID 5's balance
            String deductBalanceUrl = "http://localhost:8083/api/customers/" + customerId + "/updateCustomerBalance";
            restTemplate.put(deductBalanceUrl, totalOrderPrice.floatValue());

            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false; // Customer not found
        } catch (Exception e) {
            return false; // Other errors
        }
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public List<Order> getAllOrdersByCustomerId(Long custId) {
        return orderRepository.findByCustomerId(custId);
    }

    public void handleStockCheckResponse(StockCheckResponse response) {
        Optional<Order> optionalOrder = orderRepository.findById(response.getOrderId());

        if (optionalOrder.isEmpty()) {
            System.out.println("Order not found for ID: " + response.getOrderId());
            return;
        }

        Order order = optionalOrder.get();

        if (!response.isStockAvailable()) {
            order.setStatus("cancelled - no stock");
            order.setPaymentConfirmed(false);
            orderRepository.save(order);
            return;
        }

        boolean paymentSuccess = processPayment(order);
        if (!paymentSuccess) {
            order.setStatus("cancelled - payment failed");
            order.setPaymentConfirmed(false);
            orderRepository.save(order);
            return;
        }

        // Deduct stock (assumed to be okay now)
        deductStock(order);

        order.setStatus("confirmed");
        order.setPaymentConfirmed(true);

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : order.getItems()) {
            item.setOrder(savedOrder);
        }

        orderItemService.saveOrderItems(order.getItems());

        System.out.println("Order confirmed and saved: " + order.getId());
    }
}