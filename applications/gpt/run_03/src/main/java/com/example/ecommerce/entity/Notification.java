package com.example.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;
    private UUID orderId;
    private String customerEmail;
    private String status;
    private String message;
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(UUID orderId, String customerEmail, String status, String message) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.status = status;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getCustomerEmail() { return customerEmail; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
