package com.ecommerce.orderprocessing.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CustomerOrder(
        String orderId,
        String customerId,
        String customerEmail,
        List<OrderItem> items,
        BigDecimal totalAmount,
        String paymentMethod,
        OrderStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public CustomerOrder withStatus(OrderStatus newStatus, String reason) {
        return new CustomerOrder(
                orderId,
                customerId,
                customerEmail,
                items,
                totalAmount,
                paymentMethod,
                newStatus,
                reason,
                createdAt,
                Instant.now()
        );
    }
}
