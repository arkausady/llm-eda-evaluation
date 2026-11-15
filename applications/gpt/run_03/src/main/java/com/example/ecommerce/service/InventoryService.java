package com.example.ecommerce.service;

import com.example.ecommerce.entity.InventoryItem;
import com.example.ecommerce.model.EventType;
import com.example.ecommerce.model.InventoryRequest;
import com.example.ecommerce.model.OrderEvent;
import com.example.ecommerce.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final EventPublisher eventPublisher;

    public InventoryService(InventoryRepository inventoryRepository, EventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-service")
    @Transactional
    public void updateInventory(OrderEvent event) {
        if (event.type() != EventType.PAYMENT_COMPLETED) {
            return;
        }

        InventoryItem item = inventoryRepository.findById(event.sku()).orElse(null);
        if (item == null) {
            eventPublisher.publish("inventory-events",
                    event.withType(EventType.INVENTORY_FAILED, "Unknown SKU: " + event.sku()));
            return;
        }

        if (!item.reserve(event.quantity())) {
            eventPublisher.publish("inventory-events",
                    event.withType(EventType.INVENTORY_FAILED, "Insufficient inventory for " + event.sku()));
            return;
        }

        inventoryRepository.save(item);
        eventPublisher.publish("inventory-events", event.withType(EventType.INVENTORY_RESERVED, null));
    }

    @Transactional
    public InventoryItem replace(InventoryRequest request) {
        InventoryItem item = inventoryRepository.findById(request.sku())
                .orElseGet(() -> new InventoryItem(request.sku(), request.availableQuantity()));
        item.replaceQuantity(request.availableQuantity());
        return inventoryRepository.save(item);
    }

    @Transactional(readOnly = true)
    public InventoryItem find(String sku) {
        return inventoryRepository.findById(sku)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found: " + sku));
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> findAll() {
        return inventoryRepository.findAll();
    }
}
