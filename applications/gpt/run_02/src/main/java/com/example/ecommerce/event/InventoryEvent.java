package com.example.ecommerce.event;

import java.time.Instant;

public record InventoryEvent(
        String eventId,
        String orderId,
        String productId,
        int quantity,
        InventoryStatus status,
        String reason,
        Instant occurredAt) {
}
