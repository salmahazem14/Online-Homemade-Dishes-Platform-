package com.example.demo.controller;

import com.example.demo.model.OrderItem;
import com.example.demo.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Autowired
    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping("/getItemsByOrderId/{orderId}")
    public List<OrderItem> getItemsByOrderId(@PathVariable Long orderId) {
        return orderItemService.getItemsByOrderId(orderId);
    }

    @PostMapping("/addOrderItems")
    public List<OrderItem> addOrderItems(@RequestBody List<OrderItem> items) {
        return orderItemService.saveOrderItems(items);
    }

    @DeleteMapping("/deleteItemsByOrderId/{orderId}")
    public void deleteItemsByOrderId(@PathVariable Long orderId) {
        orderItemService.deleteItemsByOrderId(orderId);
    }
}
