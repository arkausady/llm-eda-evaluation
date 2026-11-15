package com.example.orderprocessing.event;

import java.time.LocalDateTime;

public record InventoryUpdatedEvent(
        Long orderId,
        String status,
        String message,
        LocalDateTime updatedAt
) {
}