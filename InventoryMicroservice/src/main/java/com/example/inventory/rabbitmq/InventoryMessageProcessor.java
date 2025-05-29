package com.example.inventory.rabbitmq;

import java.util.Map;

public interface InventoryMessageProcessor {
    
    /**
     * Process a stock check request and return the result
     * @param orderItems Map of dishId to quantity
     * @param companyId The company ID
     * @return true if stock is available and reserved, false otherwise
     */
    boolean processStockCheck(Map<Long, Integer> orderItems, Long companyId);
    
    /**
     * Process an order cancellation and restore stock
     * @param orderItems Map of dishId to quantity
     * @param companyId The company ID
     */
    void processOrderCancellation(Map<Long, Integer> orderItems, Long companyId);
}
