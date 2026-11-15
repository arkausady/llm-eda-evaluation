package com.ecommerce.common.event;

import com.ecommerce.common.model.OrderItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryEvent(
        String eventId,
        String orderId,
        String customerId,
        List<OrderItem> items,
        InventoryStatus status,
        String reason,
        Instant occurredAt
) {
    public static InventoryEvent reserved(PaymentEvent paymentEvent) {
        return new InventoryEvent(
                UUID.randomUUID().toString(),
                paymentEvent.orderId(),
                paymentEvent.customerId(),
                paymentEvent.items(),
                InventoryStatus.RESERVED,
                null,
                Instant.now()
        );
    }

    public static InventoryEvent failed(PaymentEvent paymentEvent, String reason) {
        return new InventoryEvent(
                UUID.randomUUID().toString(),
                paymentEvent.orderId(),
                paymentEvent.customerId(),
                paymentEvent.items(),
                InventoryStatus.FAILED,
                reason,
                Instant.now()
        );
    }
}
