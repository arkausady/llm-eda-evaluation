package com.example.ecommerce.service;

import com.example.ecommerce.config.KafkaTopics;
import com.example.ecommerce.event.InventoryEvent;
import com.example.ecommerce.event.InventoryStatus;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.event.PaymentStatus;
import com.example.ecommerce.messaging.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final int DEFAULT_STOCK = 100;
    private final EventPublisher eventPublisher;
    private final ConcurrentHashMap<String, AtomicInteger> stock = new ConcurrentHashMap<>();
    private final Set<String> processedOrders = ConcurrentHashMap.newKeySet();

    public InventoryService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "inventory-service")
    public void updateInventory(PaymentEvent event) {
        if (event.status() != PaymentStatus.COMPLETED || processedOrders.contains(event.orderId())) {
            return;
        }

        AtomicInteger available = stock.computeIfAbsent(event.productId(), ignored -> new AtomicInteger(DEFAULT_STOCK));
        boolean reserved = reserve(available, event.quantity());
        InventoryStatus status = reserved ? InventoryStatus.UPDATED : InventoryStatus.FAILED;
        String reason = reserved ? null : "Insufficient inventory for product " + event.productId();
        InventoryEvent inventoryEvent = new InventoryEvent(UUID.randomUUID().toString(), event.orderId(),
                event.productId(), event.quantity(), status, reason, Instant.now());

        try {
            eventPublisher.publish(KafkaTopics.INVENTORY_EVENTS, event.orderId(), inventoryEvent).join();
            processedOrders.add(event.orderId());
            log.info("Inventory result for order {}: {}, remaining stock={}",
                    event.orderId(), status, available.get());
        } catch (RuntimeException exception) {
            if (reserved) {
                available.addAndGet(event.quantity());
            }
            throw exception;
        }
    }

    private boolean reserve(AtomicInteger available, int quantity) {
        while (true) {
            int current = available.get();
            if (current < quantity) {
                return false;
            }
            if (available.compareAndSet(current, current - quantity)) {
                return true;
            }
        }
    }

    public int getAvailableStock(String productId) {
        return stock.computeIfAbsent(productId, ignored -> new AtomicInteger(DEFAULT_STOCK)).get();
    }
}
