package com.example.inventory.rabbitmq;

import com.example.inventory.ejb.InventoryBeanLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class InventoryMessageProcessorImpl implements InventoryMessageProcessor {
    
    private static final Logger LOGGER = Logger.getLogger(InventoryMessageProcessorImpl.class.getName());
    
    @EJB
    private InventoryBeanLocal inventoryBean;
    
    @Override
    public boolean processStockCheck(Map<Long, Integer> orderItems, Long companyId) {
        if (orderItems == null || orderItems.isEmpty()) {
            LOGGER.warning("Received empty order items for stock check");
            return false;
        }
        
        try {
            // Set the current company context
            if (companyId != null) {
                inventoryBean.setCurrentCompanyId(companyId);
            }
            
            // Check if stock is available
            boolean hasStock = inventoryBean.checkStock(orderItems);
            
            // If stock is available, reserve it
            if (hasStock) {
                hasStock = inventoryBean.reserveStock(orderItems);
                LOGGER.info("Stock reserved successfully for order items: " + orderItems);
            } else {
                LOGGER.warning("Insufficient stock for order items: " + orderItems);
            }
            
            return hasStock;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing stock check for order items: " + orderItems, e);
            return false;
        }
    }
    
    @Override
    public void processOrderCancellation(Map<Long, Integer> orderItems, Long companyId) {
        if (orderItems == null || orderItems.isEmpty()) {
            LOGGER.warning("Received empty order items for cancellation");
            return;
        }
        
        try {
            // Set the current company context
            if (companyId != null) {
                inventoryBean.setCurrentCompanyId(companyId);
            }
            
            // Restore the stock
            inventoryBean.restoreStock(orderItems);
            LOGGER.info("Stock restored for cancelled order items: " + orderItems);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing order cancellation for items: " + orderItems, e);
        }
    }
}
