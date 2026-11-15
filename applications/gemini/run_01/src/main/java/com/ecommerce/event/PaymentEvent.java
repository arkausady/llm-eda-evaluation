package com.ecommerce.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        Integer quantity,
        BigDecimal amount,
        PaymentStatus status,
        String reason,
        Instant timestamp
) {
}
