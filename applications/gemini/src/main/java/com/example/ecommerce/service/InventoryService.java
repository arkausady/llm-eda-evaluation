package com.example.ecommerce.service;

import com.example.ecommerce.event.InventoryEvent;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.model.InventoryEntity;
import com.example.ecommerce.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.inventory-events}")
    private String inventoryEventsTopic;

    public InventoryService(InventoryRepository inventoryRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processInventory(PaymentEvent paymentEvent, String productId, int quantity) {
        log.info("FR4: Updating inventory for order {}", paymentEvent.orderId());

        InventoryEntity inventory = inventoryRepository.findById(productId)
                .orElseGet(() -> new InventoryEntity(productId, 100)); // Default mock stock 100

        boolean success = false;
        String reason;

        if (inventory.getAvailableQuantity() >= quantity) {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
            inventoryRepository.save(inventory);
            success = true;
            reason = "Inventory reserved successfully";
            log.info("Reserved {} units of product {}. Remaining stock: {}", quantity, productId, inventory.getAvailableQuantity());
        } else {
            reason = "Insufficient inventory balance for product: " + productId;
            log.warn("FR6: Inventory failure for product {}. Requested: {}, Available: {}", productId, quantity, inventory.getAvailableQuantity());
        }

        String status = success ? "SUCCESS" : "FAILED";
        InventoryEvent inventoryEvent = new InventoryEvent(
                UUID.randomUUID().toString(),
                paymentEvent.orderId(),
                productId,
                quantity,
                status,
                reason,
                System.currentTimeMillis()
        );

        kafkaTemplate.send(inventoryEventsTopic, paymentEvent.orderId(), inventoryEvent);
        log.info("Published InventoryEvent for order {} with status {}", paymentEvent.orderId(), status);
    }
}