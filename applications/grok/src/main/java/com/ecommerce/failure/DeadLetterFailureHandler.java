package com.ecommerce.failure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.ecommerce.common.JsonEventMapper;
import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.order.OrderService;

@Component
public class DeadLetterFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterFailureHandler.class);

    private final OrderService orderService;
    private final JsonEventMapper jsonEventMapper;

    public DeadLetterFailureHandler(OrderService orderService, JsonEventMapper jsonEventMapper) {
        this.orderService = orderService;
        this.jsonEventMapper = jsonEventMapper;
    }

    @KafkaListener(
            topics = {
                    KafkaTopics.ORDER_EVENTS_DLT,
                    KafkaTopics.PAYMENT_EVENTS_DLT,
                    KafkaTopics.INVENTORY_EVENTS_DLT
            },
            groupId = KafkaTopics.DLT_HANDLER_GROUP
    )
    public void onDeadLetter(@Payload String payload,
                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Handling dead-letter message from topic {}: {}", topic, payload);
        String orderId = extractOrderId(topic, payload);
        if (orderId != null) {
            orderService.markProcessingError(orderId, "Message moved to dead letter topic " + topic);
        }
    }

    private String extractOrderId(String topic, String payload) {
        try {
            if (topic.startsWith(KafkaTopics.ORDER_EVENTS)) {
                return jsonEventMapper.read(payload, OrderEvent.class).getOrderId();
            }
            if (topic.startsWith(KafkaTopics.PAYMENT_EVENTS)) {
                return jsonEventMapper.read(payload, PaymentEvent.class).getOrderId();
            }
            if (topic.startsWith(KafkaTopics.INVENTORY_EVENTS)) {
                return jsonEventMapper.read(payload, InventoryEvent.class).getOrderId();
            }
        } catch (RuntimeException ex) {
            log.warn("Unable to extract order id from DLT payload on {}", topic, ex);
        }
        return null;
    }
}
