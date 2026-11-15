package com.ecommerce.notification.listener;

import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusNotificationListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public OrderStatusNotificationListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {KafkaTopics.ORDER_EVENTS, KafkaTopics.PAYMENT_EVENTS, KafkaTopics.INVENTORY_EVENTS},
            groupId = "notification-service"
    )
    public void onBusinessEvent(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            notificationService.handleEvent(topic, payload);
        } catch (Exception e) {
            log.error("Failed to generate notification from topic {} payload {}", topic, message, e);
            throw new IllegalStateException("Notification processing failed", e);
        }
    }
}
