package com.example.inventory.rest;

import com.example.inventory.ejb.InventoryBeanLocal;
import com.example.inventory.model.Dish;
import jakarta.ejb.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;
import java.util.Map;


@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {
    @EJB
    private InventoryBeanLocal bean;

//    @POST
//    @Path("/company/{companyId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response setCompany(@PathParam("companyId") Long companyId) {
//        if (companyId == null) {
//            throw new WebApplicationException("Company ID is required", 400);
//        }
//        bean.setCurrentCompanyId(companyId);
//        return Response.ok().build();
//    }

    @POST
    @Path("/AddDish/{companyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDish(@PathParam("companyId") Long companyId, Dish dish) {
        dish.setCompanyId(companyId);
        Dish createdDish = bean.createDish(dish);
        return Response.status(201)
                .entity(createdDish)
                .header("Location", "inventory/dishes/" + createdDish.getId())
                .build();
    }

    @GET
    @Path("/dishes")
    public List<Dish> all() {
        return bean.getAllDishes();
    }

    @GET
    @Path("/CompanyDishes/{id}")
    public List<Dish> getAllDishes(@PathParam("id") Long CompanyId) {
        return bean.getDishes(CompanyId);
    }

    @GET
    @Path("/dishes/{id}")
    public Dish get(@PathParam("id") Long id) {
        return bean.getDish(id);
    }

    @PUT
    @Path("/dishes/{id}")
    public Dish upd(@PathParam("id") Long id, Dish d) {
        return bean.updateDish(id, d);
    }

    @POST
    @Path("/check-stock")
    public Map<String, Boolean> chk(Map<Long, Integer> items) {
        return Map.of("ok", bean.checkStock(items));
    }

    @POST
    @Path("/reserve-stock")
    public Map<String, Boolean> res(Map<Long, Integer> items) {
        return Map.of("ok", bean.reserveStock(items));
    }

    @POST
    @Path("/restore-stock")
    public Map<String, Boolean> rst(Map<Long, Integer> items) {
        bean.restoreStock(items);
        return Map.of("ok", true);
    }

    @GET
    @Path("/check-stock/{dishId}/{qty}")
    public Map<String, Boolean> single(@PathParam("dishId") Long d, @PathParam("qty") Integer q) {
        return Map.of("ok", bean.checkStock(d, q));
    }

    @GET
    @Path("/low-stock")
    public List<Dish> low() {
        return bean.getAllDishes().stream().filter(d -> d.getStockQuantity() <= d.getLowStockThreshold()).toList();
    }
}
