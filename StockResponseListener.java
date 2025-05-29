package com.example.demo;

import com.example.demo.model.StockCheckResponse;
import com.example.demo.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.demo.model.StockCheckRequest;

@Component
public class StockResponseListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "order.stock.response")
    public void handleStockResponse(StockCheckResponse response) {
        orderService.handleStockCheckResponse(response);
    }
}
