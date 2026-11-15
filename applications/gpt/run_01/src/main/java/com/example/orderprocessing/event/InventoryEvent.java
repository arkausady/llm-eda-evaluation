package com.example.orderprocessing.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryEvent(
        UUID eventId,
        UUID orderId,
        InventoryStatus status,
        List<OrderItemData> items,
        String message,
        Instant occurredAt) {

    public enum InventoryStatus {
        RESERVED,
        FAILED
    }
}
