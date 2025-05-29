package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Endpoint to create a new order
    @PostMapping("/createOrder/{customerId}")
    public ResponseEntity<?> createOrder(
            @PathVariable Long customerId,
            @RequestBody Map<Long, Integer> dishQuantities) {

        try {
            Order order = orderService.createOrder(customerId, dishQuantities);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create order: " + e.getMessage());
        }
    }


    // Endpoint to get all orders
    @GetMapping("/getAllOrders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/getAllOrdersByCustomerId/{custId}")
    public ResponseEntity<List<Order>> getAllOrdersByCustomerId(@PathVariable Long custId) {
        List<Order> orders = orderService.getAllOrdersByCustomerId(custId);
        return ResponseEntity.ok(orders);
    }


    // Endpoint to get a specific order by ID
    @GetMapping("/getOrderById/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Endpoint to update an order
    @PutMapping("/updateOrder/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        if (!orderService.getOrderById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        order.setId(id);  // Ensure the ID matches
        Order updatedOrder = orderService.updateOrder(order);
        return ResponseEntity.ok(updatedOrder);
    }

    // Endpoint to delete an order
    @DeleteMapping("/deleteOrder/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (!orderService.getOrderById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
