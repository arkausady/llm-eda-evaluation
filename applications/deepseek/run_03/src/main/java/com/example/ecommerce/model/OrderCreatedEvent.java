package com.example.ecommerce.model;

import java.math.BigDecimal;

public class OrderCreatedEvent {
    private String orderId;
    private String customerId;
    private String productId;
    private int quantity;
    private BigDecimal amount;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String orderId, String customerId, String productId, int quantity, BigDecimal amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
