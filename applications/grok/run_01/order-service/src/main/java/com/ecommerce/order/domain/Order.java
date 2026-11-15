package com.ecommerce.order.domain;

import com.ecommerce.shared.model.OrderItem;
import com.ecommerce.shared.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Order {

    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private final Instant createdAt;
    private OrderStatus status;
    private Instant updatedAt;
    private String failureReason;

    public Order(
            String orderId,
            String customerId,
            List<OrderItem> items,
            BigDecimal totalAmount,
            OrderStatus status,
            Instant createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public synchronized void updateStatus(OrderStatus newStatus, String reason) {
        this.status = newStatus;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
