package com.example.ecommerce.notification;

import com.example.ecommerce.common.EventEnvelope;
import com.example.ecommerce.inventory.InventoryEvent;
import com.example.ecommerce.payment.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @KafkaListener(topics = "${kafka.topics.inventory-events}", groupId = "notification-group")
    public void onInventoryEvent(EventEnvelope<InventoryEvent> envelope) {
        InventoryEvent event = envelope.getPayload();
        log.info("Notification: Order {} inventory status: {}", event.getOrderId(), event.getInventoryStatus());
        // Simulate sending notification via email/SMS
    }

    @KafkaListener(topics = "${kafka.topics.payment-events}", groupId = "notification-payment-failure-group")
    public void onPaymentFailure(EventEnvelope<PaymentEvent> envelope) {
        PaymentEvent event = envelope.getPayload();
        if ("FAILED".equals(event.getPaymentStatus())) {
            log.info("Notification: Payment failed for order {} - sending failure notification", event.getOrderId());
        }
    }
}