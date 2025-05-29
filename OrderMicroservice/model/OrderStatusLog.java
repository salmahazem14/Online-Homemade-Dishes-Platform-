package com.example.demo.model;

import com.example.demo.model.Order;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_logs")
public class OrderStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status; // e.g., pending, confirmed, cancelled

    private String reason;

    private LocalDateTime changedAt;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderStatusLog() {
        this.changedAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public Order getOrder() {
        return order;
    }
}
