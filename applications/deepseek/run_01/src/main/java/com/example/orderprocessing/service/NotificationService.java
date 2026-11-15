package com.example.orderprocessing.service;

import com.example.orderprocessing.event.InventoryUpdatedEvent;
import com.example.orderprocessing.event.KafkaTopics;
import com.example.orderprocessing.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "notification-service-payment")
    public void handlePaymentEvent(PaymentProcessedEvent event) {
        if ("FAILED".equals(event.status())) {
            log.info("Notification: Payment failed for order {}: {}", event.orderId(), event.message());
        }
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "notification-service-inventory")
    public void handleInventoryEvent(InventoryUpdatedEvent event) {
        if ("SUCCESS".equals(event.status())) {
            log.info("Notification: Order {} completed successfully.", event.orderId());
        } else {
            log.info("Notification: Order {} failed during inventory update: {}", event.orderId(), event.message());
        }
    }
}