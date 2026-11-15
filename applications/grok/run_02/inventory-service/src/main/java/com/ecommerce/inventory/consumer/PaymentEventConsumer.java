package com.ecommerce.inventory.consumer;

import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.inventory.service.InventoryProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final InventoryProcessingService inventoryProcessingService;

    public PaymentEventConsumer(ObjectMapper objectMapper, InventoryProcessingService inventoryProcessingService) {
        this.objectMapper = objectMapper;
        this.inventoryProcessingService = inventoryProcessingService;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "inventory-service")
    public void onPaymentEvent(String payload) {
        try {
            PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
            log.info("Inventory service received payment event orderId={} status={}", event.orderId(), event.status());
            inventoryProcessingService.process(event);
        } catch (JsonProcessingException e) {
            throw new ProcessingException("Failed to deserialize payment event", e);
        }
    }
}
