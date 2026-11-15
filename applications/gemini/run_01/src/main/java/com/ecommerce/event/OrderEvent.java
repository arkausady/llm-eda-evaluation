package com.ecommerce.event;

import com.ecommerce.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        Integer quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        String message,
        Instant timestamp
) {
    public static OrderEvent created(UUID orderId, String customerId, String productId, Integer quantity, BigDecimal totalAmount) {
        return new OrderEvent(
                UUID.randomUUID(),
                orderId,
                customerId,
                productId,
                quantity,
                totalAmount,
                OrderStatus.CREATED,
                "Order created and awaiting payment processing",
                Instant.now()
        );
    }
}
