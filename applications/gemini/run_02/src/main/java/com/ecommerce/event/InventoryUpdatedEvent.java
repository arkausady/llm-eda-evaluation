package com.ecommerce.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryUpdatedEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        int quantity,
        boolean success,
        String reason,
        Instant timestamp
) {
}