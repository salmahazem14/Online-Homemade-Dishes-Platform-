package com.example.demo.model;

import java.util.List;

public class StockCheckRequest {
    private List<Long> dishIds;
    private List<Integer> quantities;

    public List<Long> getDishIds() {
        return dishIds;
    }

    public void setDishIds(List<Long> dishIds) {
        this.dishIds = dishIds;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }
}
