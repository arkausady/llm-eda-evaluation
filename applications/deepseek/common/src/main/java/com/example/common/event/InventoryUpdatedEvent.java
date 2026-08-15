package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdatedEvent {
    private UUID orderId;
    private UUID productId;
    private int quantity;
    private Instant timestamp;
}