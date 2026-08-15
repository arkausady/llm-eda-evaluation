package com.example.orderprocessing.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID orderId;
    private String status;
    private String message;
    private Instant createdAt;

    protected NotificationEntity() {
    }

    public NotificationEntity(UUID orderId, String status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
