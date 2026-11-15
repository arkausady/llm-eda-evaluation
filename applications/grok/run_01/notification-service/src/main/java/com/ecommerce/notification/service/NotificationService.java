package com.ecommerce.notification.service;

import com.ecommerce.notification.domain.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.shared.event.EventTypes;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification handleEvent(String topic, JsonNode payload) {
        String eventType = payload.path("eventType").asText("UNKNOWN");
        String orderId = payload.path("orderId").asText("unknown");
        String message = buildMessage(eventType, orderId, payload);

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                orderId,
                eventType,
                message,
                Instant.now()
        );
        notificationRepository.save(notification);
        log.info("Notification generated from topic {} for order {}: {}", topic, orderId, message);
        return notification;
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public List<Notification> findByOrderId(String orderId) {
        return notificationRepository.findByOrderId(orderId);
    }

    String buildMessage(String eventType, String orderId, JsonNode payload) {
        return switch (eventType) {
            case EventTypes.ORDER_CREATED -> "Order " + orderId + " was created and is awaiting payment.";
            case EventTypes.PAYMENT_COMPLETED -> "Payment completed for order " + orderId + ".";
            case EventTypes.PAYMENT_FAILED -> "Payment failed for order " + orderId + ": " + payload.path("reason").asText();
            case EventTypes.PAYMENT_REFUNDED -> "Payment refunded for order " + orderId + ": " + payload.path("reason").asText();
            case EventTypes.INVENTORY_RESERVED -> "Inventory reserved. Order " + orderId + " is confirmed.";
            case EventTypes.INVENTORY_FAILED -> "Inventory update failed for order " + orderId + ": " + payload.path("reason").asText();
            default -> "Received event " + eventType + " for order " + orderId;
        };
    }
}
