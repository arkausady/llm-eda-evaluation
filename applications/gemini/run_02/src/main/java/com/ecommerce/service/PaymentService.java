package com.ecommerce.service;

import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.InventoryUpdatedEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal MAX_ALLOWED_AMOUNT = new BigDecimal("10000.00");

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // FR3: Process payment from the received event & FR6: Handle failure
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "payment-service-group")
    public void processPayment(OrderCreatedEvent event) {
        log.info("Processing payment for order {}, customer {}, amount {}",
                event.orderId(), event.customerId(), event.totalAmount());

        boolean success = true;
        String reason = "Payment approved";

        if (event.totalAmount() == null || event.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            success = false;
            reason = "Invalid payment amount";
        } else if (event.totalAmount().compareTo(MAX_ALLOWED_AMOUNT) > 0) {
            success = false;
            reason = "Payment amount exceeds credit limit of " + MAX_ALLOWED_AMOUNT;
        } else if ("FAIL_PAYMENT".equalsIgnoreCase(event.customerId())) {
            success = false;
            reason = "Payment declined by provider for customer " + event.customerId();
        }

        PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(
                UUID.randomUUID(),
                event.orderId(),
                event.customerId(),
                event.productId(),
                event.quantity(),
                event.totalAmount(),
                success,
                reason,
                Instant.now()
        );

        kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, event.orderId().toString(), paymentEvent);
        log.info("Published PaymentProcessedEvent for order {}: success={}, reason={}",
                event.orderId(), success, reason);
    }

    // FR6: Compensating action if inventory fails after payment succeeded
    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "payment-service-group")
    public void handleInventoryFailureCompensation(InventoryUpdatedEvent event) {
        if (!event.success()) {
            log.info("Compensating transaction: Initiating payment refund for order {} due to inventory failure: {}",
                    event.orderId(), event.reason());
        }
    }
}