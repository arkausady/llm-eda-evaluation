package com.example.ecommerce.event;

public record InventoryEvent(
    String inventoryId,
    String orderId,
    String productId,
    int quantity,
    String status,
    String reason,
    long timestamp
) {}