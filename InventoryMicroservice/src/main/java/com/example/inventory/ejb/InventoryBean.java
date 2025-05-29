package com.example.inventory.ejb;

import com.example.inventory.model.Dish;
import jakarta.ejb.*;
import jakarta.persistence.*;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.*;
import java.util.List;
import java.util.Map;

@Stateless
public class InventoryBean implements InventoryBeanLocal {
    @PersistenceContext(unitName="inventoryPU")
    private EntityManager em;
    private static final String USER_SERVICE_URL="http://localhost:8081/api/companies/";
    private Long currentCompanyId;

    /**
     * Set the current company ID for all operations
     */
    @Override
    public void setCurrentCompanyId(Long companyId) {
        Client client = ClientBuilder.newClient();
        int status = client.target(USER_SERVICE_URL + companyId).request().get().getStatus();
        if (status != 200) {
            throw new WebApplicationException("Invalid companyId", 400);
        }
        this.currentCompanyId = companyId;
    }

    /**
     * Get the current company ID
     */
    @Override
    public Long getCurrentCompanyId() {
        return currentCompanyId;
    }

    @Override
    public Dish createDish(Dish dish) {
        em.persist(dish);
        return dish;
    }

    /**
     * Get all dishes for the current company
     */
    @Override
    public List<Dish> getDishes(Long id) {
        currentCompanyId = id;
        if (currentCompanyId == null) {
            throw new WebApplicationException("No company selected", 400);
        }
        return em.createQuery(
                "SELECT d FROM Dish d WHERE d.companyId = :cid", Dish.class)
                .setParameter("cid", currentCompanyId)
                .getResultList();
    }

    /**
     * Update a dish for the current company
     */
    @Override
    public Dish updateDish(Long id, Dish u) {
        Dish existing = em.find(Dish.class, id);
        if (existing == null) {
            throw new WebApplicationException(404);
        }
        existing.setName(u.getName());
        existing.setDescription(u.getDescription());
        existing.setPrice(u.getPrice());
        existing.setStockQuantity(u.getStockQuantity());
        return existing;
    }

    @Override
    public Dish getDish(Long id) {
        Dish d = em.find(Dish.class, id);
        if (d == null) {
            throw new WebApplicationException(404);
        }
        if (currentCompanyId != null && !d.getCompanyId().equals(currentCompanyId)) {
            throw new WebApplicationException("Dish does not belong to current company", 403);
        }
        return d;
    }

    @Override
    public List<Dish> getAllDishes() {
        return em.createQuery("SELECT d FROM Dish d", Dish.class).getResultList();
    }


    @Override
    public boolean checkStock(Map<Long, Integer> items) {
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Dish dish = em.find(Dish.class, entry.getKey());
            if (dish == null) return false;
            if (currentCompanyId != null && !dish.getCompanyId().equals(currentCompanyId)) {
                throw new WebApplicationException("Dish does not belong to current company", 403);
            }
            if (dish.getStockQuantity() < entry.getValue()) return false;
        }
        return true;
    }

    @Override
    public boolean checkStock(Long dishId, Integer quantity) {
        Dish dish = em.find(Dish.class, dishId);
        if (dish == null) return false;
        if (currentCompanyId != null && !dish.getCompanyId().equals(currentCompanyId)) {
            throw new WebApplicationException("Dish does not belong to current company", 403);
        }
        return dish.getStockQuantity() >= quantity;
    }

    @Override
    public boolean reserveStock(Map<Long, Integer> items) {
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Dish dish = em.find(Dish.class, entry.getKey());
            if (dish == null) return false;
            if (currentCompanyId != null && !dish.getCompanyId().equals(currentCompanyId)) {
                throw new WebApplicationException("Dish does not belong to current company", 403);
            }
            dish.setStockQuantity(dish.getStockQuantity() - entry.getValue());
        }
        return true;
    }

    @Override
    public void restoreStock(Map<Long, Integer> items) {
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Dish dish = em.find(Dish.class, entry.getKey());
            if (dish != null) {
                if (currentCompanyId != null && !dish.getCompanyId().equals(currentCompanyId)) {
                    throw new WebApplicationException("Dish does not belong to current company", 403);
                }
                dish.setStockQuantity(dish.getStockQuantity() + entry.getValue());
            }
        }
    }
}
