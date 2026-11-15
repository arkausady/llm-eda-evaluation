package com.ecommerce.orderprocessing.model;

import java.math.BigDecimal;

public record InventoryItem(
        String productId,
        String productName,
        int availableQuantity,
        BigDecimal unitPrice
) {
    public InventoryItem decrement(int quantity) {
        return new InventoryItem(productId, productName, availableQuantity - quantity, unitPrice);
    }
}
