package com.example.orderprocessing.service;

import com.example.orderprocessing.event.InventoryEvent;
import com.example.orderprocessing.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final EventPublisher eventPublisher;

    public InventoryService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-service")
    public void updateInventory(PaymentEvent paymentEvent) {
        if (paymentEvent.status() == PaymentEvent.PaymentStatus.FAILED) {
            log.info("Ignoring inventory update for failed payment on order {}", paymentEvent.orderId());
            return;
        }

        var unavailableItem = paymentEvent.items().stream()
                .filter(item -> item.productId().toUpperCase().startsWith("OUT-OF-STOCK") || item.quantity() > 100)
                .findFirst();
        var reserved = unavailableItem.isEmpty();
        var message = reserved
                ? "Inventory reserved successfully"
                : "Insufficient inventory for product " + unavailableItem.orElseThrow().productId();
        var inventoryEvent = new InventoryEvent(
                UUID.randomUUID(),
                paymentEvent.orderId(),
                reserved ? InventoryEvent.InventoryStatus.RESERVED : InventoryEvent.InventoryStatus.FAILED,
                paymentEvent.items(),
                message,
                Instant.now());
        eventPublisher.publish("inventory-events", paymentEvent.orderId().toString(), inventoryEvent);
    }
}
