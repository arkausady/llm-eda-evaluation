package com.ecommerce.events;

import java.time.Instant;
import java.util.UUID;

public record InventoryEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        int quantity,
        InventoryStatus status,
        String reason,
        Instant timestamp
) {
    public enum InventoryStatus {
        RESERVED,
        OUT_OF_STOCK
    }

    public static InventoryEvent reserved(PaymentEvent paymentEvent) {
        return new InventoryEvent(
                UUID.randomUUID(),
                paymentEvent.orderId(),
                paymentEvent.customerId(),
                paymentEvent.productId(),
                paymentEvent.quantity(),
                InventoryStatus.RESERVED,
                "Inventory reserved successfully",
                Instant.now()
        );
    }

    public static InventoryEvent outOfStock(PaymentEvent paymentEvent, String reason) {
        return new InventoryEvent(
                UUID.randomUUID(),
                paymentEvent.orderId(),
                paymentEvent.customerId(),
                paymentEvent.productId(),
                paymentEvent.quantity(),
                InventoryStatus.OUT_OF_STOCK,
                reason,
                Instant.now()
        );
    }
}
