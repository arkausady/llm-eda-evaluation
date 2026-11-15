package com.example.orderprocessing.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentEvent(
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        PaymentStatus status,
        BigDecimal amount,
        List<OrderItemData> items,
        String message,
        Instant occurredAt) {

    public enum PaymentStatus {
        SUCCEEDED,
        FAILED
    }
}
