package com.example.orderprocessing.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String message;
    private Instant processedAt;

    protected PaymentEntity() {
    }

    public PaymentEntity(UUID orderId, BigDecimal amount, PaymentStatus status, String message) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.message = message;
        this.processedAt = Instant.now();
    }

    public UUID getOrderId() { return orderId; }
    public PaymentStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
