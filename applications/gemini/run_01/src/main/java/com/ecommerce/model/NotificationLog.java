package com.ecommerce.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Instant timestamp;

    public NotificationLog() {
    }

    public NotificationLog(UUID orderId, String customerId, String status, String message) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
