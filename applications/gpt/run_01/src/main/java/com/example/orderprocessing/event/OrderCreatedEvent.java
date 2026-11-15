package com.example.orderprocessing.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String customerEmail,
        String paymentToken,
        BigDecimal totalAmount,
        List<OrderItemData> items,
        Instant occurredAt) {
}
