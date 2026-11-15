package com.ecommerce.notification.consumer;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.notification.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumers {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public EventConsumers(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "notification-service")
    public void onOrderCreated(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            notificationService.notifyOrderCreated(event);
        } catch (JsonProcessingException e) {
            throw new ProcessingException("Failed to deserialize order-created event", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "notification-service")
    public void onPayment(String payload) {
        try {
            PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
            notificationService.notifyPayment(event);
        } catch (JsonProcessingException e) {
            throw new ProcessingException("Failed to deserialize payment event", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "notification-service")
    public void onInventory(String payload) {
        try {
            InventoryEvent event = objectMapper.readValue(payload, InventoryEvent.class);
            notificationService.notifyInventory(event);
        } catch (JsonProcessingException e) {
            throw new ProcessingException("Failed to deserialize inventory event", e);
        }
    }
}
