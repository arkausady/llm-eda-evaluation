package com.example.ecommerce.inventory;

import com.example.ecommerce.model.PaymentProcessedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private final InventoryService inventoryService;

    public InventoryConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-group")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        if ("SUCCESS".equals(event.getStatus())) {
            inventoryService.updateInventory(event);
        }
    }
}
