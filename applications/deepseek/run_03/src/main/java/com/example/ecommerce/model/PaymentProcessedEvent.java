package com.example.ecommerce.model;

import java.math.BigDecimal;

public class PaymentProcessedEvent {
    private String orderId;
    private String status; // SUCCESS or FAILED
    private String message;
    private BigDecimal amount;

    public PaymentProcessedEvent() {}

    public PaymentProcessedEvent(String orderId, String status, String message, BigDecimal amount) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.amount = amount;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
