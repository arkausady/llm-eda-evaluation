package com.example.orderprocessing.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "stock")
public class StockEntity {
    @Id
    private String sku;
    private int availableQuantity;
    @Version
    private long version;

    protected StockEntity() {
    }

    public StockEntity(String sku, int availableQuantity) {
        this.sku = sku;
        this.availableQuantity = availableQuantity;
    }

    public void reserve(int quantity) {
        if (quantity < 1 || availableQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock for SKU " + sku);
        }
        availableQuantity -= quantity;
    }

    public String getSku() { return sku; }
    public int getAvailableQuantity() { return availableQuantity; }
}
