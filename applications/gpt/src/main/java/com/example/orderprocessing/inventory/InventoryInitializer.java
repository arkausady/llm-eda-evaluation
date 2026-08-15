package com.example.orderprocessing.inventory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InventoryInitializer implements CommandLineRunner {
    private final StockRepository repository;

    public InventoryInitializer(StockRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        createIfMissing("SKU-100", 100);
        createIfMissing("SKU-200", 50);
        createIfMissing("SKU-300", 25);
    }

    private void createIfMissing(String sku, int quantity) {
        if (!repository.existsById(sku)) {
            repository.save(new StockEntity(sku, quantity));
        }
    }
}
