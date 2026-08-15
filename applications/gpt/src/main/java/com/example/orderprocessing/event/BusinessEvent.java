package com.example.orderprocessing.event;

import com.example.orderprocessing.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BusinessEvent(
        UUID eventId,
        UUID orderId,
        EventType type,
        Instant occurredAt,
        BigDecimal amount,
        List<OrderItem> items,
        String paymentToken,
        String message) {

    public BusinessEvent {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static BusinessEvent orderCreated(UUID orderId, BigDecimal amount,
                                             List<OrderItem> items, String paymentToken) {
        return new BusinessEvent(UUID.randomUUID(), orderId, EventType.ORDER_CREATED,
                Instant.now(), amount, items, paymentToken, "Order created");
    }

    public BusinessEvent next(EventType newType, String newMessage) {
        return new BusinessEvent(UUID.randomUUID(), orderId, newType, Instant.now(),
                amount, items, null, newMessage);
    }
}
