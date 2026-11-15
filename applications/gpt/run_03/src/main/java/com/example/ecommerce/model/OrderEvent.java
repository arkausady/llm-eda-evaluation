package com.example.ecommerce.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderEvent(
        UUID eventId,
        UUID orderId,
        EventType type,
        String customerEmail,
        String sku,
        int quantity,
        BigDecimal amount,
        String paymentMethodId,
        String reason,
        Instant occurredAt
) {
    public static OrderEvent orderCreated(UUID orderId, String customerEmail, String sku, int quantity,
                                          BigDecimal amount, String paymentMethodId) {
        return new OrderEvent(UUID.randomUUID(), orderId, EventType.ORDER_CREATED, customerEmail, sku,
                quantity, amount, paymentMethodId, null, Instant.now());
    }

    public OrderEvent withType(EventType newType, String newReason) {
        return new OrderEvent(UUID.randomUUID(), orderId, newType, customerEmail, sku, quantity,
                amount, paymentMethodId, newReason, Instant.now());
    }
}
