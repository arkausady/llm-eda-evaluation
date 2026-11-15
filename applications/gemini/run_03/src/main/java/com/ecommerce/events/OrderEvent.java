package com.ecommerce.events;

import com.ecommerce.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        String message,
        Instant timestamp
) {
    public static OrderEvent created(UUID orderId, String customerId, String productId, int quantity, BigDecimal totalAmount) {
        return new OrderEvent(
                UUID.randomUUID(),
                orderId,
                customerId,
                productId,
                quantity,
                totalAmount,
                OrderStatus.CREATED,
                "Order created successfully",
                Instant.now()
        );
    }
}
