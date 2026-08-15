package com.ecommerce.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.ecommerce.common.JsonEventMapper;
import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.InventoryEventType;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.OrderEventType;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentService paymentService;
    private final JsonEventMapper jsonEventMapper;

    public PaymentEventConsumer(PaymentService paymentService, JsonEventMapper jsonEventMapper) {
        this.paymentService = paymentService;
        this.jsonEventMapper = jsonEventMapper;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = KafkaTopics.PAYMENT_SERVICE_GROUP)
    public void onOrderEvent(@Payload String payload) {
        OrderEvent event = jsonEventMapper.read(payload, OrderEvent.class);
        if (event.getEventType() != OrderEventType.ORDER_CREATED) {
            return;
        }
        log.info("Payment service received ORDER_CREATED for {}", event.getOrderId());
        paymentService.processOrderCreated(event);
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = KafkaTopics.PAYMENT_SERVICE_GROUP)
    public void onInventoryEvent(@Payload String payload) {
        InventoryEvent event = jsonEventMapper.read(payload, InventoryEvent.class);
        if (event.getEventType() == InventoryEventType.INVENTORY_FAILED && event.getOrder() != null) {
            log.info("Compensating payment for order {} after inventory failure", event.getOrderId());
            paymentService.refund(event.getOrder(), "Inventory reservation failed: " + event.getReason());
        }
    }
}
