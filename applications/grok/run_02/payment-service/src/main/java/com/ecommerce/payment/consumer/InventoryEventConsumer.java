package com.ecommerce.payment.consumer;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.event.InventoryStatus;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.payment.service.PaymentProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final PaymentProcessingService paymentProcessingService;

    public InventoryEventConsumer(ObjectMapper objectMapper, PaymentProcessingService paymentProcessingService) {
        this.objectMapper = objectMapper;
        this.paymentProcessingService = paymentProcessingService;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "payment-service")
    public void onInventoryEvent(String payload) {
        try {
            InventoryEvent event = objectMapper.readValue(payload, InventoryEvent.class);
            if (event.status() == InventoryStatus.FAILED) {
                log.warn("Inventory failed for order {}, starting payment compensation", event.orderId());
                paymentProcessingService.compensate(event.orderId(), event.reason());
            }
        } catch (JsonProcessingException e) {
            throw new ProcessingException("Failed to deserialize inventory event", e);
        }
    }
}
