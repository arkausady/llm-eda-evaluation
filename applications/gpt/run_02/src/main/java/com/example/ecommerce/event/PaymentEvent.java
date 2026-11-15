package com.example.ecommerce.event;

import java.time.Instant;

public record PaymentEvent(
        String eventId,
        String orderId,
        String productId,
        int quantity,
        PaymentStatus status,
        String reason,
        Instant occurredAt) {
}
