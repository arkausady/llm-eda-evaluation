package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.stock.InventoryStock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryStock inventoryStock;

    public InventoryController(InventoryStock inventoryStock) {
        this.inventoryStock = inventoryStock;
    }

    @GetMapping
    public Map<String, Integer> snapshot() {
        return inventoryStock.snapshot();
    }

    @GetMapping("/{productId}")
    public Map<String, Object> available(@PathVariable String productId) {
        return Map.of("productId", productId, "available", inventoryStock.available(productId));
    }
}
