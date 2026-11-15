package com.example.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    private String sku;

    private int availableQuantity;

    @Version
    private long version;

    protected InventoryItem() {
    }

    public InventoryItem(String sku, int availableQuantity) {
        this.sku = sku;
        this.availableQuantity = availableQuantity;
    }

    public boolean reserve(int quantity) {
        if (quantity <= 0 || availableQuantity < quantity) {
            return false;
        }
        availableQuantity -= quantity;
        return true;
    }

    public void replaceQuantity(int quantity) {
        this.availableQuantity = quantity;
    }

    public String getSku() { return sku; }
    public int getAvailableQuantity() { return availableQuantity; }
    public long getVersion() { return version; }
}
