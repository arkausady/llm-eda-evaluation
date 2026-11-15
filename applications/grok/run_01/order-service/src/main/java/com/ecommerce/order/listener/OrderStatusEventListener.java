package com.ecommerce.order.listener;

import com.ecommerce.order.service.OrderService;
import com.ecommerce.shared.event.InventoryEvent;
import com.ecommerce.shared.event.PaymentEvent;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusEventListener.class);

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderStatusEventListener(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "order-service")
    public void onPaymentEvent(String message) {
        try {
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            orderService.applyPaymentEvent(event);
        } catch (Exception e) {
            log.error("Failed to process payment event payload: {}", message, e);
            throw new IllegalStateException("Payment event processing failed", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "order-service")
    public void onInventoryEvent(String message) {
        try {
            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);
            orderService.applyInventoryEvent(event);
        } catch (Exception e) {
            log.error("Failed to process inventory event payload: {}", message, e);
            throw new IllegalStateException("Inventory event processing failed", e);
        }
    }
}
