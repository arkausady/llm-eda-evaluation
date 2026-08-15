package com.ecommerce.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.ecommerce.common.JsonEventMapper;
import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentEventType;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final InventoryService inventoryService;
    private final JsonEventMapper jsonEventMapper;

    public InventoryEventConsumer(InventoryService inventoryService, JsonEventMapper jsonEventMapper) {
        this.inventoryService = inventoryService;
        this.jsonEventMapper = jsonEventMapper;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = KafkaTopics.INVENTORY_SERVICE_GROUP)
    public void onPaymentEvent(@Payload String payload) {
        PaymentEvent event = jsonEventMapper.read(payload, PaymentEvent.class);
        if (event.getEventType() != PaymentEventType.PAYMENT_COMPLETED) {
            return;
        }
        log.info("Inventory service received PAYMENT_COMPLETED for {}", event.getOrderId());
        inventoryService.reserveForPaidOrder(event);
    }
}
