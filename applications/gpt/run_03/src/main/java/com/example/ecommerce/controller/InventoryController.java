package com.example.ecommerce.controller;

import com.example.ecommerce.entity.InventoryItem;
import com.example.ecommerce.model.InventoryRequest;
import com.example.ecommerce.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PutMapping
    public InventoryItem replace(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.replace(request);
    }

    @GetMapping("/{sku}")
    public InventoryItem find(@PathVariable String sku) {
        return inventoryService.find(sku);
    }

    @GetMapping
    public List<InventoryItem> findAll() {
        return inventoryService.findAll();
    }
}
