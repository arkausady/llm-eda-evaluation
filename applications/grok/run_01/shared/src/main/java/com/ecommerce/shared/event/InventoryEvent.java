package com.ecommerce.shared.event;

import com.ecommerce.shared.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryEvent(
        String eventId,
        String eventType,
        String orderId,
        String customerId,
        List<OrderItem> items,
        String reason,
        Instant timestamp
) {
    public static InventoryEvent reserved(String orderId, String customerId, List<OrderItem> items) {
        return new InventoryEvent(
                UUID.randomUUID().toString(),
                EventTypes.INVENTORY_RESERVED,
                orderId,
                customerId,
                items,
                null,
                Instant.now()
        );
    }

    public static InventoryEvent failed(String orderId, String customerId, List<OrderItem> items, String reason) {
        return new InventoryEvent(
                UUID.randomUUID().toString(),
                EventTypes.INVENTORY_FAILED,
                orderId,
                customerId,
                items,
                reason,
                Instant.now()
        );
    }
}
