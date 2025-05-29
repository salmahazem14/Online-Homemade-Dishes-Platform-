package com.example.demo.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class StockCheckResponse {
    private Long orderId;
    private boolean stockAvailable;
    private Long unavailableDishId; // null if all items are in stock
    private String message;
    private Map<Long, BigDecimal> dishPrices = new HashMap<>();
    private BigDecimal totalAmount;

    public StockCheckResponse() {}

    public StockCheckResponse(boolean stockAvailable, Long unavailableDishId, String message, 
                            Map<Long, BigDecimal> dishPrices, BigDecimal totalAmount) {
        this.stockAvailable = stockAvailable;
        this.unavailableDishId = unavailableDishId;
        this.message = message;
        if (dishPrices != null) {
            this.dishPrices = new HashMap<>(dishPrices);
        }
        this.totalAmount = totalAmount;
    }

    public boolean isStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(boolean stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    public Long getUnavailableDishId() {
        return unavailableDishId;
    }

    public void setUnavailableDishId(Long unavailableDishId) {
        this.unavailableDishId = unavailableDishId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Map<Long, BigDecimal> getDishPrices() {
        return dishPrices;
    }

    public void setDishPrices(Map<Long, BigDecimal> dishPrices) {
        this.dishPrices = dishPrices != null ? new HashMap<>(dishPrices) : new HashMap<>();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
