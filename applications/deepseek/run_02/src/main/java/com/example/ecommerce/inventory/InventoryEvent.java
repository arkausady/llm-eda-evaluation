package com.example.ecommerce.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryEvent {
    private String orderId;
    private String inventoryStatus; // UPDATED, FAILED
    private long timestamp;
}