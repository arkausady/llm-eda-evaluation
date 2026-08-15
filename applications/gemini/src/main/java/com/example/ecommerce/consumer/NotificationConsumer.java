package com.example.ecommerce.consumer;

import com.example.ecommerce.event.InventoryEvent;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "notification-group")
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        if ("FAILED".equals(paymentEvent.status())) {
            notificationService.sendOrderNotification(paymentEvent.orderId(), "PAYMENT_FAILED", paymentEvent.reason());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.inventory-events}", groupId = "notification-group")
    public void handleInventoryEvent(InventoryEvent inventoryEvent) {
        String status = "SUCCESS".equals(inventoryEvent.status()) ? "ORDER_COMPLETED" : "INVENTORY_FAILED";
        notificationService.sendOrderNotification(inventoryEvent.orderId(), status, inventoryEvent.reason());
    }
}