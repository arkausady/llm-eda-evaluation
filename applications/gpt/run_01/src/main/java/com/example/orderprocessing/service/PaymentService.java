package com.example.orderprocessing.service;

import com.example.orderprocessing.event.OrderCreatedEvent;
import com.example.orderprocessing.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal MAXIMUM_PAYMENT = new BigDecimal("10000.00");

    private final EventPublisher eventPublisher;

    public PaymentService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void process(OrderCreatedEvent orderEvent) {
        log.info("Processing payment for order {}", orderEvent.orderId());
        var failure = validatePayment(orderEvent);
        var succeeded = failure == null;
        var paymentEvent = new PaymentEvent(
                UUID.randomUUID(),
                orderEvent.orderId(),
                succeeded ? UUID.randomUUID() : null,
                succeeded ? PaymentEvent.PaymentStatus.SUCCEEDED : PaymentEvent.PaymentStatus.FAILED,
                orderEvent.totalAmount(),
                orderEvent.items(),
                succeeded ? "Payment processed successfully" : failure,
                Instant.now());
        eventPublisher.publish("payment-events", orderEvent.orderId().toString(), paymentEvent);
    }

    private String validatePayment(OrderCreatedEvent event) {
        if (event.paymentToken() == null || event.paymentToken().isBlank()
                || event.paymentToken().toUpperCase().startsWith("FAIL")) {
            return "Payment was declined";
        }
        if (event.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Payment amount must be positive";
        }
        if (event.totalAmount().compareTo(MAXIMUM_PAYMENT) > 0) {
            return "Payment exceeds the allowed transaction limit";
        }
        return null;
    }
}
