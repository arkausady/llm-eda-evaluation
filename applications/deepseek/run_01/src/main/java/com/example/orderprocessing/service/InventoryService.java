package com.example.orderprocessing.service;

import com.example.orderprocessing.event.InventoryUpdatedEvent;
import com.example.orderprocessing.event.KafkaTopics;
import com.example.orderprocessing.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    public InventoryService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "inventory-service")
    public void updateInventory(PaymentProcessedEvent event) {
        if (!"SUCCESS".equals(event.status())) {
            log.info("Skipping inventory update for order {} due to payment status {}", event.orderId(), event.status());
            return;
        }
        try {
            boolean success = random.nextDouble() > 0.05; // 95% success rate
            String status = success ? "SUCCESS" : "FAILED";
            String message = success ? "Inventory updated successfully" : "Insufficient stock";

            InventoryUpdatedEvent inventoryEvent = new InventoryUpdatedEvent(
                    event.orderId(),
                    status,
                    message,
                    LocalDateTime.now()
            );
            kafkaTemplate.send(KafkaTopics.INVENTORY_EVENTS, String.valueOf(event.orderId()), inventoryEvent);
            log.info("Published InventoryUpdatedEvent for order {} with status {}", event.orderId(), status);
        } catch (Exception e) {
            log.error("Error updating inventory for order {}: {}", event.orderId(), e.getMessage(), e);
            InventoryUpdatedEvent failedEvent = new InventoryUpdatedEvent(
                    event.orderId(),
                    "FAILED",
                    "Inventory update error: " + e.getMessage(),
                    LocalDateTime.now()
            );
            try {
                kafkaTemplate.send(KafkaTopics.INVENTORY_EVENTS, String.valueOf(event.orderId()), failedEvent);
                log.info("Published failure InventoryUpdatedEvent for order {}", event.orderId());
            } catch (Exception ex) {
                log.error("Failed to publish inventory failure event for order {}", event.orderId(), ex);
            }
        }
    }
}