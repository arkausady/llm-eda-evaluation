package com.ecommerce.payment.consumer;

import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.payment.service.PaymentProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final PaymentProcessingService paymentProcessingService;

    public OrderEventConsumer(ObjectMapper objectMapper, PaymentProcessingService paymentProcessingService) {
        this.objectMapper = objectMapper;
        this.paymentProcessingService = paymentProcessingService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "payment-service")
    public void onOrderCreated(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("Payment service received order-created event orderId={}", event.orderId());
            paymentProcessingService.process(event);
        } catch (JsonProcessingException e) {
            throw new ProcessingException("Failed to deserialize order-created event", e);
        }
    }
}
