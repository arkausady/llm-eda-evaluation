package com.example.ecommerce.api;

import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String id,
        String customerId,
        String productId,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getProductId(),
                order.getQuantity(), order.getTotalAmount(), order.getStatus(),
                order.getFailureReason(), order.getCreatedAt(), order.getUpdatedAt());
    }
}
