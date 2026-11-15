package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.domain.StockItem;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StockRepository {

    private final ConcurrentHashMap<String, StockItem> stock = new ConcurrentHashMap<>();

    public StockRepository() {
        save(new StockItem("PROD-1", "Laptop", 100));
        save(new StockItem("PROD-2", "Mouse", 50));
        save(new StockItem("PROD-3", "Keyboard", 10));
    }

    public void save(StockItem item) {
        stock.put(item.getProductId(), item);
    }

    public Optional<StockItem> findByProductId(String productId) {
        return Optional.ofNullable(stock.get(productId));
    }

    public List<StockItem> findAll() {
        return new ArrayList<>(stock.values());
    }
}
