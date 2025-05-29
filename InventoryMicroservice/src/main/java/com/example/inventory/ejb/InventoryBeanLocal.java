package com.example.inventory.ejb;

import com.example.inventory.model.Dish;
import java.util.List;
import java.util.Map;

public interface InventoryBeanLocal {
    void setCurrentCompanyId(Long companyId);
    Long getCurrentCompanyId();
    Dish createDish(Dish dish);

    List<Dish> getDishes(Long id);

    Dish updateDish(Long id, Dish dish);
    Dish getDish(Long id);
    List<Dish> getAllDishes();
    boolean checkStock(Map<Long, Integer> items);
    boolean checkStock(Long dishId, Integer quantity);
    boolean reserveStock(Map<Long, Integer> items);
    void restoreStock(Map<Long, Integer> items);
}
