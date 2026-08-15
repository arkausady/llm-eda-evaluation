package com.example.notification.kafka;

import com.example.common.event.*;
import com.example.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        notificationService.notifyOrderCreated(event);
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        notificationService.notifyPaymentProcessed(event);
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void consumePaymentFailed(PaymentFailedEvent event) {
        notificationService.notifyPaymentFailed(event);
    }

    @KafkaListener(topics = "inventory-events", groupId = "notification-service")
    public void consumeInventoryUpdated(InventoryUpdatedEvent event) {
        notificationService.notifyInventoryUpdated(event);
    }

    @KafkaListener(topics = "inventory-events", groupId = "notification-service")
    public void consumeInventoryFailed(InventoryFailedEvent event) {
        notificationService.notifyInventoryFailed(event);
    }
}
