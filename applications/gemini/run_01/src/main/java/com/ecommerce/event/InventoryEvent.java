package com.ecommerce.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        Integer quantity,
        InventoryStatus status,
        String reason,
        Instant timestamp
) {
}
