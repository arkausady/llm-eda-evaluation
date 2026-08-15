package com.ecommerce.inventory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.ecommerce.model.InventoryItem;
import jakarta.annotation.PostConstruct;

@Repository
public class InventoryRepository {

    private final ConcurrentHashMap<String, InventoryItem> stock = new ConcurrentHashMap<>();

    @PostConstruct
    public void seed() {
        save(new InventoryItem("SKU-001", "Wireless Headphones", 100, new BigDecimal("79.99")));
        save(new InventoryItem("SKU-002", "USB-C Charger", 50, new BigDecimal("24.50")));
        save(new InventoryItem("SKU-003", "Limited Edition Keyboard", 2, new BigDecimal("199.00")));
    }

    public InventoryItem save(InventoryItem item) {
        stock.put(item.getSku(), item);
        return item;
    }

    public Optional<InventoryItem> findBySku(String sku) {
        return Optional.ofNullable(stock.get(sku));
    }

    public Collection<InventoryItem> findAll() {
        return stock.values();
    }

    public void reset() {
        stock.clear();
        seed();
    }
}
