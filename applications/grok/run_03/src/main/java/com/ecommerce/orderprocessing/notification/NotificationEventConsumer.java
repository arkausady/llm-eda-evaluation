package com.ecommerce.orderprocessing.notification;

import com.ecommerce.orderprocessing.config.Topics;
import com.ecommerce.orderprocessing.event.EventParser;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final EventParser eventParser;

    public NotificationEventConsumer(NotificationService notificationService, EventParser eventParser) {
        this.notificationService = notificationService;
        this.eventParser = eventParser;
    }

    @KafkaListener(
            topics = {Topics.ORDER_EVENTS, Topics.PAYMENT_EVENTS, Topics.INVENTORY_EVENTS},
            groupId = "notification-service")
    public void onBusinessEvent(String payload) {
        JsonNode node = eventParser.readTree(payload);
        String eventType = node.path("eventType").asText("UNKNOWN");
        String orderId = node.path("orderId").asText();
        String email = node.path("customerEmail").asText();
        String message = buildMessage(eventType, node);
        notificationService.notify(orderId, email, eventType, message);
    }

    private String buildMessage(String eventType, JsonNode node) {
        return switch (eventType) {
            case "ORDER_CREATED" -> "Order received and queued for payment.";
            case "PAYMENT_PROCESSED" -> "Payment completed successfully.";
            case "PAYMENT_FAILED" -> "Payment failed: " + node.path("message").asText();
            case "INVENTORY_UPDATED" -> "Inventory reserved. Order is complete.";
            case "INVENTORY_FAILED" -> "Inventory update failed: " + node.path("message").asText();
            default -> {
                log.warn("Unhandled event type {}", eventType);
                yield "Order status update: " + eventType;
            }
        };
    }
}
