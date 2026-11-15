package com.example.ecommerce.notification;

import com.example.ecommerce.model.InventoryUpdatedEvent;
import com.example.ecommerce.model.PaymentProcessedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void consumePayment(PaymentProcessedEvent event) {
        if ("FAILED".equals(event.getStatus())) {
            notificationService.sendPaymentFailureNotification(event);
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "notification-group")
    public void consumeInventory(InventoryUpdatedEvent event) {
        notificationService.sendInventoryNotification(event);
    }
}
