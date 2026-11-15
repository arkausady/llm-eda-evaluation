package com.ecommerce.service;

import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.InventoryStatus;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentStatus;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.repository.InventoryRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String inventoryEventsTopic;

    public InventoryService(InventoryRepository inventoryRepository,
                            KafkaTemplate<String, Object> kafkaTemplate,
                            @Value("${app.kafka.topics.inventory-events}") String inventoryEventsTopic) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryEventsTopic = inventoryEventsTopic;
    }

    @PostConstruct
    public void initDefaultInventory() {
        if (inventoryRepository.count() == 0) {
            inventoryRepository.saveAll(List.of(
                    new InventoryItem("PROD-100", 50),
                    new InventoryItem("PROD-200", 10),
                    new InventoryItem("PROD-300", 5),
                    new InventoryItem("PROD-OUT", 0)
            ));
            log.info("Initialized default product inventory.");
        }
    }

    /**
     * FR4: Update inventory after order payment processing.
     * FR6: Handle out-of-stock failure appropriately and publish failure event.
     */
    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "inventory-service-group")
    public void updateInventory(PaymentEvent paymentEvent) {
        if (paymentEvent.status() != PaymentStatus.SUCCESS) {
            return;
        }

        log.info("InventoryService checking stock for orderId: {}, productId: {}, requested: {}",
                paymentEvent.orderId(), paymentEvent.productId(), paymentEvent.quantity());

        Optional<InventoryItem> itemOpt = inventoryRepository.findById(paymentEvent.productId());
        InventoryEvent inventoryEvent;

        if (itemOpt.isEmpty()) {
            inventoryEvent = new InventoryEvent(
                    UUID.randomUUID(),
                    paymentEvent.orderId(),
                    paymentEvent.customerId(),
                    paymentEvent.productId(),
                    paymentEvent.quantity(),
                    InventoryStatus.OUT_OF_STOCK,
                    "Product not found in inventory: " + paymentEvent.productId(),
                    Instant.now()
            );
            log.warn("Product {} not found for orderId {}", paymentEvent.productId(), paymentEvent.orderId());
        } else {
            InventoryItem item = itemOpt.get();
            if (item.getAvailableQuantity() < paymentEvent.quantity()) {
                inventoryEvent = new InventoryEvent(
                        UUID.randomUUID(),
                        paymentEvent.orderId(),
                        paymentEvent.customerId(),
                        paymentEvent.productId(),
                        paymentEvent.quantity(),
                        InventoryStatus.OUT_OF_STOCK,
                        "Insufficient stock for product " + paymentEvent.productId()
                                + ". Available: " + item.getAvailableQuantity()
                                + ", Requested: " + paymentEvent.quantity(),
                        Instant.now()
                );
                log.warn("Insufficient stock for orderId {}: available={}, requested={}",
                        paymentEvent.orderId(), item.getAvailableQuantity(), paymentEvent.quantity());
            } else {
                item.setAvailableQuantity(item.getAvailableQuantity() - paymentEvent.quantity());
                inventoryRepository.save(item);
                inventoryEvent = new InventoryEvent(
                        UUID.randomUUID(),
                        paymentEvent.orderId(),
                        paymentEvent.customerId(),
                        paymentEvent.productId(),
                        paymentEvent.quantity(),
                        InventoryStatus.RESERVED,
                        "Inventory reserved successfully",
                        Instant.now()
                );
                log.info("Inventory updated for product {}. Remaining quantity: {}",
                        item.getProductId(), item.getAvailableQuantity());
            }
        }

        kafkaTemplate.send(inventoryEventsTopic, paymentEvent.orderId().toString(), inventoryEvent);
    }

    public List<InventoryItem> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Transactional
    public InventoryItem setInventoryStock(String productId, int quantity) {
        InventoryItem item = inventoryRepository.findById(productId)
                .orElse(new InventoryItem(productId, quantity));
        item.setAvailableQuantity(quantity);
        return inventoryRepository.save(item);
    }
}
