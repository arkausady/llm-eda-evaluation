package com.ecommerce.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomerOrder {

    private String orderId;
    private String customerId;
    private String customerEmail;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentMethod;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public CustomerOrder() {
    }

    public static CustomerOrder create(String customerId,
                                       String customerEmail,
                                       List<OrderItem> items,
                                       String paymentMethod) {
        CustomerOrder order = new CustomerOrder();
        order.orderId = UUID.randomUUID().toString();
        order.customerId = customerId;
        order.customerEmail = customerEmail;
        order.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        order.totalAmount = order.items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.status = OrderStatus.CREATED;
        order.paymentMethod = paymentMethod == null || paymentMethod.isBlank() ? "CARD" : paymentMethod;
        Instant now = Instant.now();
        order.createdAt = now;
        order.updatedAt = now;
        return order;
    }

    public void mark(OrderStatus newStatus, String reason) {
        this.status = newStatus;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
