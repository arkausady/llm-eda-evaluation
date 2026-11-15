package com.ecommerce.orderprocessing.order;

import com.ecommerce.orderprocessing.config.Topics;
import com.ecommerce.orderprocessing.event.EventParser;
import com.ecommerce.orderprocessing.event.InventoryEvent;
import com.ecommerce.orderprocessing.event.PaymentEvent;
import com.ecommerce.orderprocessing.model.OrderStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusConsumer.class);

    private final OrderService orderService;
    private final EventParser eventParser;

    public OrderStatusConsumer(OrderService orderService, EventParser eventParser) {
        this.orderService = orderService;
        this.eventParser = eventParser;
    }

    @KafkaListener(topics = Topics.PAYMENT_EVENTS, groupId = "order-service")
    public void onPaymentEvent(String payload) {
        JsonNode node = eventParser.readTree(payload);
        String type = node.path("eventType").asText();
        if (PaymentEvent.PROCESSED.equals(type) || PaymentEvent.FAILED.equals(type)) {
            PaymentEvent event = eventParser.read(payload, PaymentEvent.class);
            if (event.success()) {
                orderService.updateStatus(event.orderId(), OrderStatus.PAID, event.message());
            } else {
                orderService.updateStatus(event.orderId(), OrderStatus.PAYMENT_FAILED, event.message());
            }
        } else {
            log.debug("Ignoring payment topic event type {}", type);
        }
    }

    @KafkaListener(topics = Topics.INVENTORY_EVENTS, groupId = "order-service")
    public void onInventoryEvent(String payload) {
        JsonNode node = eventParser.readTree(payload);
        String type = node.path("eventType").asText();
        if (InventoryEvent.UPDATED.equals(type) || InventoryEvent.FAILED.equals(type)) {
            InventoryEvent event = eventParser.read(payload, InventoryEvent.class);
            if (event.success()) {
                orderService.updateStatus(event.orderId(), OrderStatus.COMPLETED, event.message());
            } else {
                orderService.updateStatus(event.orderId(), OrderStatus.INVENTORY_FAILED, event.message());
            }
        } else {
            log.debug("Ignoring inventory topic event type {}", type);
        }
    }
}
