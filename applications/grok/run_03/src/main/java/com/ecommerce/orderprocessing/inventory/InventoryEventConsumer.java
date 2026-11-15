package com.ecommerce.orderprocessing.inventory;

import com.ecommerce.orderprocessing.config.Topics;
import com.ecommerce.orderprocessing.event.EventParser;
import com.ecommerce.orderprocessing.event.InventoryEvent;
import com.ecommerce.orderprocessing.event.PaymentEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;
    private final EventParser eventParser;

    public InventoryEventConsumer(
            InventoryService inventoryService,
            InventoryEventProducer inventoryEventProducer,
            EventParser eventParser) {
        this.inventoryService = inventoryService;
        this.inventoryEventProducer = inventoryEventProducer;
        this.eventParser = eventParser;
    }

    @KafkaListener(topics = Topics.PAYMENT_EVENTS, groupId = "inventory-service")
    public void onPaymentEvent(String payload) {
        JsonNode node = eventParser.readTree(payload);
        String type = node.path("eventType").asText();
        if (!PaymentEvent.PROCESSED.equals(type)) {
            log.info("Skipping inventory update for event type {}", type);
            return;
        }
        PaymentEvent paymentEvent = eventParser.read(payload, PaymentEvent.class);
        try {
            InventoryEvent inventoryEvent = inventoryService.process(paymentEvent);
            inventoryEventProducer.publish(inventoryEvent);
        } catch (Exception e) {
            log.error("Inventory processing failed for order {}", paymentEvent.orderId(), e);
            InventoryEvent failure = new InventoryEvent(
                    UUID.randomUUID().toString(),
                    InventoryEvent.FAILED,
                    Instant.now(),
                    paymentEvent.orderId(),
                    paymentEvent.customerId(),
                    paymentEvent.customerEmail(),
                    paymentEvent.items() == null ? List.of() : paymentEvent.items(),
                    false,
                    "Inventory processing error: " + e.getMessage()
            );
            inventoryEventProducer.publish(failure);
        }
    }
}
