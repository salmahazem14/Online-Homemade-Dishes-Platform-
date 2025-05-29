package com.example.inventory.rabbitmq;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJB;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class RabbitMQConfig {
    
    private static final Logger LOGGER = Logger.getLogger(RabbitMQConfig.class.getName());
    
    // RabbitMQ connection parameters
    private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";
    
    // Exchange and Queue names
    public static final String ORDER_EXCHANGE = "order.events";
    public static final String INVENTORY_EXCHANGE = "inventory.events";
    public static final String STOCK_CHECK_QUEUE = "inventory.stock.check";
    public static final String STOCK_CHECK_ROUTING_KEY = "stock.check";
    public static final String STOCK_RESPONSE_ROUTING_KEY = "stock.response";
    
    private Connection connection;
    private Channel channel;
    
    @EJB
    private InventoryMessageProcessor messageProcessor;
    
    @PostConstruct
    public void init() {
        try {
            // Create connection factory
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setPort(PORT);
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            
            // Create connection and channel
            connection = factory.newConnection("inventory-service");
            channel = connection.createChannel();
            
            // Declare exchanges
            channel.exchangeDeclare(ORDER_EXCHANGE, "topic", true);
            channel.exchangeDeclare(INVENTORY_EXCHANGE, "topic", true);
            
            // Declare and bind the stock check queue
            channel.queueDeclare(STOCK_CHECK_QUEUE, true, false, false, null);
            channel.queueBind(STOCK_CHECK_QUEUE, ORDER_EXCHANGE, STOCK_CHECK_ROUTING_KEY);
            
            // Set up consumer for stock check requests
            channel.basicConsume(STOCK_CHECK_QUEUE, false, new StockCheckConsumer(channel, messageProcessor));
            
            LOGGER.info("RabbitMQ initialized successfully");
            
        } catch (IOException | TimeoutException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize RabbitMQ", e);
            throw new RuntimeException("Failed to initialize RabbitMQ", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.log(Level.WARNING, "Error closing RabbitMQ resources", e);
        }
    }
}
