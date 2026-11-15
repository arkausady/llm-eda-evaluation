package com.ecommerce.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentProcessedEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal totalAmount,
        boolean success,
        String reason,
        Instant timestamp
) {
}