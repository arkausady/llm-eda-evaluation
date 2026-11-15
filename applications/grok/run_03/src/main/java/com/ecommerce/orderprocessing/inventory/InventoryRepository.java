package com.ecommerce.orderprocessing.inventory;

import com.ecommerce.orderprocessing.model.InventoryItem;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InventoryRepository {

    private final ConcurrentHashMap<String, InventoryItem> stock = new ConcurrentHashMap<>();

    public InventoryRepository() {
        save(new InventoryItem("PROD-001", "Laptop", 10, new BigDecimal("999.99")));
        save(new InventoryItem("PROD-002", "Mouse", 50, new BigDecimal("29.99")));
        save(new InventoryItem("PROD-003", "Keyboard", 30, new BigDecimal("79.99")));
    }

    public InventoryItem save(InventoryItem item) {
        stock.put(item.productId(), item);
        return item;
    }

    public Optional<InventoryItem> findById(String productId) {
        return Optional.ofNullable(stock.get(productId));
    }

    public List<InventoryItem> findAll() {
        return List.copyOf(stock.values());
    }
}
