package com.example.inventory.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StockCheckConsumer extends DefaultConsumer {
    
    private static final Logger LOGGER = Logger.getLogger(StockCheckConsumer.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InventoryMessageProcessor messageProcessor;
    private final Channel channel;
    
    public StockCheckConsumer(Channel channel, InventoryMessageProcessor messageProcessor) {
        super(channel);
        this.channel = channel;
        this.messageProcessor = messageProcessor;
    }
    
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, 
                             AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, StandardCharsets.UTF_8);
        String correlationId = properties.getCorrelationId();
        String replyTo = properties.getReplyTo();
        
        // Initialize variables
        Long orderId = null;
        Long companyId = null;
        Map<Long, Integer> orderItems = new HashMap<>();
        
        try {
            // Parse the request
            Map<String, Object> request = objectMapper.readValue(
                message, 
                new TypeReference<Map<String, Object>>() {}
            );
            
            // Extract payload
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.get("payload");
            
            if (payload != null) {
                // Extract order ID and company ID
                orderId = payload.get("orderId") != null ? 
                    ((Number) payload.get("orderId")).longValue() : null;
                companyId = payload.get("companyId") != null ? 
                    ((Number) payload.get("companyId")).longValue() : null;
                
                // Extract and convert order items
                @SuppressWarnings("unchecked")
                Map<String, Object> itemsMap = (Map<String, Object>) payload.get("items");
                
                if (itemsMap != null) {
                    itemsMap.forEach((key, value) -> {
                        try {
                            Long itemId = Long.parseLong(key);
                            Integer quantity = ((Number) value).intValue();
                            orderItems.put(itemId, quantity);
                        } catch (NumberFormatException e) {
                            LOGGER.log(Level.WARNING, "Invalid item ID format: " + key, e);
                        }
                    });
                }
            }
            
            // Process the stock check (synchronously for now)
            try {
                // Log the incoming request
                LOGGER.info("Processing stock check for order: " + orderId + 
                          ", company: " + companyId + 
                          ", items: " + orderItems);
                
                // Process the stock check
                boolean isAvailable = messageProcessor.processStockCheck(orderItems, companyId);
                
                // Prepare the response
                Map<String, Object> response = new HashMap<>();
                response.put("success", isAvailable);
                response.put("orderId", orderId);
                response.put("message", isAvailable ? "Stock is available" : "Insufficient stock");
                
                // Convert response to JSON
                String responseJson = objectMapper.writeValueAsString(response);
                
                // Send the response back to the reply-to queue
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationId)
                    .contentType("application/json")
                    .build();
                    
                channel.basicPublish("", replyTo, replyProps, responseJson.getBytes(StandardCharsets.UTF_8));
                channel.basicAck(envelope.getDeliveryTag(), false);
                
                LOGGER.info("Sent stock check response for order: " + orderId + 
                          ", success: " + isAvailable);
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing stock check for order: " + orderId, e);
                try {
                    // Send error response
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("orderId", orderId);
                    errorResponse.put("message", "Error processing stock check: " + e.getMessage());
                    
                    String errorJson = objectMapper.writeValueAsString(errorResponse);
                    
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(correlationId)
                        .contentType("application/json")
                        .build();
                        
                    channel.basicPublish("", replyTo, replyProps, errorJson.getBytes(StandardCharsets.UTF_8));
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    
                } catch (Exception ioEx) {
                    LOGGER.log(Level.SEVERE, "Error sending error response for order: " + orderId, ioEx);
                    try {
                        // Negative acknowledgment - don't requeue the message
                        channel.basicNack(envelope.getDeliveryTag(), false, false);
                    } catch (IOException nackEx) {
                        LOGGER.log(Level.SEVERE, "Error sending NACK for order: " + orderId, nackEx);
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing stock check request", e);
            // Negative acknowledge the message and requeue it
            channel.basicNack(envelope.getDeliveryTag(), false, true);
        }
    }
}
