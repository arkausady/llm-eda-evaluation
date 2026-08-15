package com.example.inventory.service;

import com.example.common.event.*;
import com.example.inventory.kafka.InventoryEventPublisher;
import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventPublisher eventPublisher;

    public InventoryService(InventoryRepository inventoryRepository, InventoryEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    public void updateInventory(PaymentProcessedEvent paymentEvent) {
        UUID orderId = paymentEvent.getOrderId();
        // For simplicity, assume quantity = 1 per order. Could be enhanced.
        UUID productId = UUID.fromString("1-1-1-1-1"); // dummy, but we could use a productId from somewhere
        // We'll just get productId from a lookup or set as constant. In real scenario, we'd know from order.
        // Since we don't have order details, we'll assume productId is in a placeholder.
        // Instead, we'll just create a dummy productId from order ID? Better to use a fixed product ID for demo.
        productId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        int quantity = 1;

        InventoryItem item = inventoryRepository.findById(productId).orElseGet(() -> {
            InventoryItem newItem = new InventoryItem();
            newItem.setProductId(productId);
            newItem.setQuantity(100); // initial stock
            return newItem;
        });

        if (item.getQuantity() >= quantity) {
            item.setQuantity(item.getQuantity() - quantity);
            inventoryRepository.save(item);

            InventoryUpdatedEvent event = new InventoryUpdatedEvent(
                orderId,
                productId,
                quantity,
                Instant.now()
            );
            eventPublisher.publishInventoryUpdated(event);
        } else {
            InventoryFailedEvent event = new InventoryFailedEvent(
                orderId,
                productId,
                "Insufficient stock",
                Instant.now()
            );
            eventPublisher.publishInventoryFailed(event);
        }
    }
}
