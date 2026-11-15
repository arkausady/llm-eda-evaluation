package com.ecommerce.payment.listener;

import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.shared.event.EventTypes;
import com.ecommerce.shared.event.InventoryEvent;
import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListeners {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListeners.class);

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentEventListeners(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "payment-service")
    public void onOrderEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String type = node.path("eventType").asText();
            if (!EventTypes.ORDER_CREATED.equals(type)) {
                log.debug("Ignoring order event type {}", type);
                return;
            }
            OrderCreatedEvent event = objectMapper.treeToValue(node, OrderCreatedEvent.class);
            paymentService.handleOrderCreated(event);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to consume order event: {}", message, e);
            throw new IllegalStateException("Order event consumption failed", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "payment-service")
    public void onInventoryEvent(String message) {
        try {
            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);
            if (EventTypes.INVENTORY_FAILED.equals(event.eventType())) {
                paymentService.handleInventoryFailed(event);
            }
        } catch (Exception e) {
            log.error("Failed to consume inventory event: {}", message, e);
            throw new IllegalStateException("Inventory event consumption failed", e);
        }
    }
}
