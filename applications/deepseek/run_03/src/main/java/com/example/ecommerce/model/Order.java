package com.example.ecommerce.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Order {
    private String orderId;
    private String customerId;
    private String productId;
    private int quantity;
    private BigDecimal amount;
    private OrderStatus status;

    public Order() {
        this.orderId = UUID.randomUUID().toString();
        this.status = OrderStatus.CREATED;
    }

    public Order(String customerId, String productId, int quantity, BigDecimal amount) {
        this();
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
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public enum OrderStatus {
        CREATED, PAYMENT_SUCCESS, PAYMENT_FAILED, INVENTORY_UPDATED, NOTIFICATION_SENT
    }
}
