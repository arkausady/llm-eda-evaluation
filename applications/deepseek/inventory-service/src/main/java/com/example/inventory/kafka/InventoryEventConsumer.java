package com.example.inventory.kafka;

import com.example.common.event.PaymentProcessedEvent;
import com.example.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventConsumer {

    private final InventoryService inventoryService;

    public InventoryEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-service")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        if (event.isSuccess()) {
            inventoryService.updateInventory(event);
        }
    }
}
