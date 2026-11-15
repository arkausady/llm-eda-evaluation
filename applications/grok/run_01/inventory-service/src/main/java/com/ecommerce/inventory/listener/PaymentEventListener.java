package com.ecommerce.inventory.listener;

import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.shared.event.EventTypes;
import com.ecommerce.shared.event.PaymentEvent;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    public PaymentEventListener(InventoryService inventoryService, ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "inventory-service")
    public void onPaymentEvent(String message) {
        try {
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            if (!EventTypes.PAYMENT_COMPLETED.equals(event.eventType())) {
                log.debug("Skipping payment event type {} for order {}", event.eventType(), event.orderId());
                return;
            }
            inventoryService.reserveForPayment(event);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to consume payment event: {}", message, e);
            throw new IllegalStateException("Payment event consumption failed", e);
        }
    }
}
