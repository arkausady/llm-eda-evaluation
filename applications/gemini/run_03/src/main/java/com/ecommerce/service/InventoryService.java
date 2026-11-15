package com.ecommerce.service;

import com.ecommerce.config.KafkaConfig;
import com.ecommerce.events.InventoryEvent;
import com.ecommerce.events.PaymentEvent;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.repository.InventoryRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        if (inventoryRepository.count() == 0) {
            inventoryRepository.saveAll(List.of(
                    new InventoryItem("PROD-101", 100),
                    new InventoryItem("PROD-102", 50),
                    new InventoryItem("PROD-103", 5),
                    new InventoryItem("PROD-OUT", 0)
            ));
            log.info("Initialized default product inventory.");
        }
    }

    /**
     * FR4: Update inventory after order processing (payment success).
     * FR6: Handle out-of-stock failure.
     */
    @KafkaListener(topics = KafkaConfig.PAYMENT_EVENTS_TOPIC, groupId = "inventory-service-group")
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        if (event.status() != PaymentEvent.PaymentStatus.SUCCESS) {
            return;
        }

        log.info("Checking inventory for orderId: {}, productId: {}, requested quantity: {}",
                event.orderId(), event.productId(), event.quantity());

        Optional<InventoryItem> itemOpt = inventoryRepository.findById(event.productId());

        InventoryEvent inventoryEvent;
        if (itemOpt.isEmpty()) {
            log.warn("Product {} not found in inventory for orderId: {}", event.productId(), event.orderId());
            inventoryEvent = InventoryEvent.outOfStock(event, "Product not found in inventory: " + event.productId());
        } else {
            InventoryItem item = itemOpt.get();
            if (item.getAvailableQuantity() < event.quantity()) {
                log.warn("Insufficient stock for product {}. Available: {}, Requested: {}, orderId: {}",
                        event.productId(), item.getAvailableQuantity(), event.quantity(), event.orderId());
                inventoryEvent = InventoryEvent.outOfStock(event,
                        "Insufficient stock for product " + event.productId() + ". Available: " + item.getAvailableQuantity());
            } else {
                item.setAvailableQuantity(item.getAvailableQuantity() - event.quantity());
                inventoryRepository.save(item);
                log.info("Reserved {} units of product {} for orderId: {}. Remaining stock: {}",
                        event.quantity(), event.productId(), event.orderId(), item.getAvailableQuantity());
                inventoryEvent = InventoryEvent.reserved(event);
            }
        }

        kafkaTemplate.send(KafkaConfig.INVENTORY_EVENTS_TOPIC, event.orderId().toString(), inventoryEvent);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Transactional
    public InventoryItem addOrUpdateInventory(String productId, int quantity) {
        InventoryItem item = inventoryRepository.findById(productId)
                .orElse(new InventoryItem(productId, 0));
        item.setAvailableQuantity(quantity);
        return inventoryRepository.save(item);
    }
}
