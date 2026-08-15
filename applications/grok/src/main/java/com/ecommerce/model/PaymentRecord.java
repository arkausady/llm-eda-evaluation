package com.ecommerce.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentRecord {

    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String failureReason;
    private Instant createdAt;

    public PaymentRecord() {
    }

    public static PaymentRecord of(String orderId, BigDecimal amount, String paymentMethod, PaymentStatus status, String failureReason) {
        PaymentRecord record = new PaymentRecord();
        record.paymentId = UUID.randomUUID().toString();
        record.orderId = orderId;
        record.amount = amount;
        record.paymentMethod = paymentMethod;
        record.status = status;
        record.failureReason = failureReason;
        record.createdAt = Instant.now();
        return record;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
