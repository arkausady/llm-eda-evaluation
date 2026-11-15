package com.ecommerce.orderprocessing.event;

import com.ecommerce.orderprocessing.model.OrderItem;

import java.time.Instant;
import java.util.List;

public record InventoryEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String orderId,
        String customerId,
        String customerEmail,
        List<OrderItem> items,
        boolean success,
        String message
) {
    public static final String UPDATED = "INVENTORY_UPDATED";
    public static final String FAILED = "INVENTORY_FAILED";
}
