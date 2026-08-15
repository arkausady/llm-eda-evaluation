package com.example.orderprocessing.order;

import com.example.orderprocessing.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        BigDecimal amount,
        List<OrderItem> items,
        OrderStatus status,
        String statusMessage,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(OrderEntity order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getAmount(),
                order.toOrderItems(), order.getStatus(), order.getStatusMessage(),
                order.getCreatedAt(), order.getUpdatedAt());
    }
}
