package com.example.ecommerce.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String eventId,
        String orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal totalAmount,
        String paymentReference,
        Instant occurredAt) {
}
