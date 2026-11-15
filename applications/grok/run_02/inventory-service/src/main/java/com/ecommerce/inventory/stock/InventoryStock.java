package com.ecommerce.inventory.stock;

import com.ecommerce.common.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InventoryStock {

    private static final int DEFAULT_STOCK = 100;
    private final ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();

    public InventoryStock() {
        stock.put("SKU-001", 100);
        stock.put("SKU-002", 50);
        stock.put("SKU-003", 0);
    }

    public synchronized boolean reserve(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (available(item.productId()) < item.quantity()) {
                return false;
            }
        }
        for (OrderItem item : items) {
            int current = available(item.productId());
            stock.put(item.productId(), current - item.quantity());
        }
        return true;
    }

    public int available(String productId) {
        return stock.getOrDefault(productId, DEFAULT_STOCK);
    }

    public Map<String, Integer> snapshot() {
        return new LinkedHashMap<>(stock);
    }
}
