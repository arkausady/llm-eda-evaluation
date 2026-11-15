package com.ecommerce.orderprocessing.payment;

import com.ecommerce.orderprocessing.config.Topics;
import com.ecommerce.orderprocessing.event.EventParser;
import com.ecommerce.orderprocessing.event.OrderCreatedEvent;
import com.ecommerce.orderprocessing.event.PaymentEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;
    private final EventParser eventParser;

    public PaymentEventConsumer(
            PaymentService paymentService,
            PaymentEventProducer paymentEventProducer,
            EventParser eventParser) {
        this.paymentService = paymentService;
        this.paymentEventProducer = paymentEventProducer;
        this.eventParser = eventParser;
    }

    @KafkaListener(topics = Topics.ORDER_EVENTS, groupId = "payment-service")
    public void onOrderEvent(String payload) {
        JsonNode node = eventParser.readTree(payload);
        String type = node.path("eventType").asText();
        if (!OrderCreatedEvent.TYPE.equals(type)) {
            log.debug("Payment service ignoring event type {}", type);
            return;
        }
        OrderCreatedEvent orderEvent = eventParser.read(payload, OrderCreatedEvent.class);
        try {
            PaymentEvent paymentEvent = paymentService.process(orderEvent);
            paymentEventProducer.publish(paymentEvent);
        } catch (Exception e) {
            log.error("Payment processing failed for order {}", orderEvent.orderId(), e);
            PaymentEvent failure = new PaymentEvent(
                    UUID.randomUUID().toString(),
                    PaymentEvent.FAILED,
                    Instant.now(),
                    orderEvent.orderId(),
                    orderEvent.customerId(),
                    orderEvent.customerEmail(),
                    orderEvent.items() == null ? List.of() : orderEvent.items(),
                    orderEvent.totalAmount(),
                    orderEvent.paymentMethod(),
                    false,
                    "Payment processing error: " + e.getMessage()
            );
            paymentEventProducer.publish(failure);
        }
    }
}
