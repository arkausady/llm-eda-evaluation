package com.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;

public class OrderResponse {

    private String orderId;
    private String customerId;
    private String customerEmail;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentMethod;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderResponse from(CustomerOrder order) {
        OrderResponse response = new OrderResponse();
        response.orderId = order.getOrderId();
        response.customerId = order.getCustomerId();
        response.customerEmail = order.getCustomerEmail();
        response.items = order.getItems();
        response.totalAmount = order.getTotalAmount();
        response.status = order.getStatus();
        response.paymentMethod = order.getPaymentMethod();
        response.failureReason = order.getFailureReason();
        response.createdAt = order.getCreatedAt();
        response.updatedAt = order.getUpdatedAt();
        return response;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
