package com.ecommerce.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.ecommerce.common.JsonEventMapper;
import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.PaymentEvent;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final JsonEventMapper jsonEventMapper;

    public NotificationEventConsumer(NotificationService notificationService, JsonEventMapper jsonEventMapper) {
        this.notificationService = notificationService;
        this.jsonEventMapper = jsonEventMapper;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = KafkaTopics.NOTIFICATION_SERVICE_GROUP)
    public void onOrderEvent(@Payload String payload) {
        OrderEvent event = jsonEventMapper.read(payload, OrderEvent.class);
        log.info("Notification service received {} for {}", event.getEventType(), event.getOrderId());
        notificationService.notifyOrderEvent(event);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = KafkaTopics.NOTIFICATION_SERVICE_GROUP)
    public void onPaymentEvent(@Payload String payload) {
        PaymentEvent event = jsonEventMapper.read(payload, PaymentEvent.class);
        log.info("Notification service received {} for {}", event.getEventType(), event.getOrderId());
        notificationService.notifyPaymentEvent(event);
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = KafkaTopics.NOTIFICATION_SERVICE_GROUP)
    public void onInventoryEvent(@Payload String payload) {
        InventoryEvent event = jsonEventMapper.read(payload, InventoryEvent.class);
        log.info("Notification service received {} for {}", event.getEventType(), event.getOrderId());
        notificationService.notifyInventoryEvent(event);
    }
}
