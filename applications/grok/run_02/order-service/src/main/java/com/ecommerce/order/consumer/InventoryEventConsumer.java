package com.ecommerce.order.consumer;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.order.service.OrderService;
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
    private final OrderService orderService;

    public InventoryEventConsumer(ObjectMapper objectMapper, OrderService orderService) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "order-service")
    public void onInventoryEvent(String payload) {
        try {
            InventoryEvent event = objectMapper.readValue(payload, InventoryEvent.class);
            log.info("Order service received inventory event orderId={} status={}", event.orderId(), event.status());
            orderService.applyInventory(event);
        } catch (JsonProcessingException e) {
            throw new ProcessingException("Failed to deserialize inventory event", e);
        }
    }
}
