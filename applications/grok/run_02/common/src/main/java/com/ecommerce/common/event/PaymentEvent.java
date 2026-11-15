package com.ecommerce.common.event;

import com.ecommerce.common.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentEvent(
        String eventId,
        String orderId,
        String customerId,
        List<OrderItem> items,
        BigDecimal amount,
        PaymentStatus status,
        String reason,
        Instant occurredAt
) {
    public static PaymentEvent success(OrderCreatedEvent orderEvent) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                orderEvent.orderId(),
                orderEvent.customerId(),
                orderEvent.items(),
                orderEvent.totalAmount(),
                PaymentStatus.SUCCESS,
                null,
                Instant.now()
        );
    }

    public static PaymentEvent failed(OrderCreatedEvent orderEvent, String reason) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                orderEvent.orderId(),
                orderEvent.customerId(),
                orderEvent.items(),
                orderEvent.totalAmount(),
                PaymentStatus.FAILED,
                reason,
                Instant.now()
        );
    }
}
