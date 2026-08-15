package com.ecommerce.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.ecommerce.common.JsonEventMapper;
import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.PaymentEvent;

@Component
public class OrderStatusConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusConsumer.class);

    private final OrderService orderService;
    private final JsonEventMapper jsonEventMapper;

    public OrderStatusConsumer(OrderService orderService, JsonEventMapper jsonEventMapper) {
        this.orderService = orderService;
        this.jsonEventMapper = jsonEventMapper;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = KafkaTopics.ORDER_SERVICE_GROUP)
    public void onPaymentEvent(@Payload String payload) {
        PaymentEvent event = jsonEventMapper.read(payload, PaymentEvent.class);
        log.info("Order service received {} for order {}", event.getEventType(), event.getOrderId());
        orderService.handlePaymentEvent(event);
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = KafkaTopics.ORDER_SERVICE_GROUP)
    public void onInventoryEvent(@Payload String payload) {
        InventoryEvent event = jsonEventMapper.read(payload, InventoryEvent.class);
        log.info("Order service received {} for order {}", event.getEventType(), event.getOrderId());
        orderService.handleInventoryEvent(event);
    }
}
