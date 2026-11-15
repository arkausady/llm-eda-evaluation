package com.ecommerce.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String statusType;

    @Column(nullable = false)
    private Instant sentAt;

    public NotificationLog() {
    }

    public NotificationLog(UUID orderId, String customerId, String message, String statusType) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.message = message;
        this.statusType = statusType;
        this.sentAt = Instant.now();
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

    public String getMessage() {
        return message;
    }

    public String getStatusType() {
        return statusType;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
