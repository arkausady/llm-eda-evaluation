package com.ecommerce.model;

import java.math.BigDecimal;

public class InventoryItem {

    private String sku;
    private String name;
    private int availableQuantity;
    private BigDecimal unitPrice;

    public InventoryItem() {
    }

    public InventoryItem(String sku, String name, int availableQuantity, BigDecimal unitPrice) {
        this.sku = sku;
        this.name = name;
        this.availableQuantity = availableQuantity;
        this.unitPrice = unitPrice;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
