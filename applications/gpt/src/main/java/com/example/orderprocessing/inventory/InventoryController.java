package com.example.orderprocessing.inventory;

import com.example.orderprocessing.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final StockRepository repository;

    public InventoryController(StockRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{sku}")
    public InventoryResponse find(@PathVariable String sku) {
        StockEntity stock = repository.findById(sku)
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + sku));
        return new InventoryResponse(stock.getSku(), stock.getAvailableQuantity());
    }

    public record InventoryResponse(String sku, int availableQuantity) {
    }
}
