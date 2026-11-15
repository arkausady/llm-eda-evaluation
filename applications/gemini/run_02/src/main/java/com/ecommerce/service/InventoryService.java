package com.ecommerce.service;

import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.InventoryUpdatedEvent;
import com.ecommerce.event.PaymentProcessedEvent;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.repository.InventoryRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryService(InventoryRepository inventoryRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void initDefaultInventory() {
        inventoryRepository.save(new InventoryItem("PROD-100", 50));
        inventoryRepository.save(new InventoryItem("PROD-200", 10));
        inventoryRepository.save(new InventoryItem("PROD-OUT-OF-STOCK", 0));
    }

    // FR4: Update inventory after order processing & FR6: Handle processing failure
    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "inventory-service-group")
    @Transactional
    public void updateInventory(PaymentProcessedEvent event) {
        if (!event.success()) {
            log.info("Skipping inventory update for order {} because payment failed.", event.orderId());
            return;
        }

        log.info("Checking inventory for order {}, product {}, quantity {}",
                event.orderId(), event.productId(), event.quantity());

        Optional<InventoryItem> itemOpt = inventoryRepository.findById(event.productId());
        boolean success = false;
        String reason;

        if (itemOpt.isEmpty()) {
            reason = "Product not found in inventory: " + event.productId();
        } else {
            InventoryItem item = itemOpt.get();
            if (item.getAvailableQuantity() >= event.quantity()) {
                item.setAvailableQuantity(item.getAvailableQuantity() - event.quantity());
                inventoryRepository.save(item);
                success = true;
                reason = "Inventory reserved successfully";
            } else {
                reason = "Insufficient stock for product " + event.productId()
                        + ". Requested: " + event.quantity() + ", Available: " + item.getAvailableQuantity();
            }
        }

        InventoryUpdatedEvent inventoryEvent = new InventoryUpdatedEvent(
                UUID.randomUUID(),
                event.orderId(),
                event.customerId(),
                event.productId(),
                event.quantity(),
                success,
                reason,
                Instant.now()
        );

        kafkaTemplate.send(KafkaTopics.INVENTORY_EVENTS, event.orderId().toString(), inventoryEvent);
        log.info("Published InventoryUpdatedEvent for order {}: success={}, reason={}",
                event.orderId(), success, reason);
    }
}