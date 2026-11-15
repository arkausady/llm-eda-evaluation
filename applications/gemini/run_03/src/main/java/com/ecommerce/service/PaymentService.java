package com.ecommerce.service;

import com.ecommerce.config.KafkaConfig;
import com.ecommerce.events.InventoryEvent;
import com.ecommerce.events.OrderEvent;
import com.ecommerce.events.PaymentEvent;
import com.ecommerce.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal MAX_ALLOWED_AMOUNT = new BigDecimal("10000.00");

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * FR3: Process payment from the received event.
     * FR6: Handle processing failure appropriately.
     */
    @KafkaListener(topics = KafkaConfig.ORDER_EVENTS_TOPIC, groupId = "payment-service-group")
    public void processOrderPayment(OrderEvent event) {
        if (event.status() != OrderStatus.CREATED) {
            return;
        }

        log.info("Processing payment for orderId: {}, amount: {}", event.orderId(), event.totalAmount());

        PaymentEvent paymentEvent;

        // Simulate payment failure conditions (FR6)
        if (event.totalAmount().compareTo(MAX_ALLOWED_AMOUNT) > 0) {
            log.warn("Payment failed for orderId: {}. Amount {} exceeds limit {}",
                    event.orderId(), event.totalAmount(), MAX_ALLOWED_AMOUNT);
            paymentEvent = PaymentEvent.failed(event, "Payment amount exceeds maximum limit of " + MAX_ALLOWED_AMOUNT);
        } else if ("FAIL_PAYMENT".equalsIgnoreCase(event.customerId())) {
            log.warn("Payment declined for customerId: {} on orderId: {}", event.customerId(), event.orderId());
            paymentEvent = PaymentEvent.failed(event, "Payment declined by payment gateway for customer");
        } else {
            log.info("Payment succeeded for orderId: {}", event.orderId());
            paymentEvent = PaymentEvent.success(event);
        }

        kafkaTemplate.send(KafkaConfig.PAYMENT_EVENTS_TOPIC, event.orderId().toString(), paymentEvent);
    }

    /**
     * FR6: Compensating transaction - refund payment if inventory reservation fails.
     */
    @KafkaListener(topics = KafkaConfig.INVENTORY_EVENTS_TOPIC, groupId = "payment-service-group")
    public void handleInventoryFailureCompensation(InventoryEvent event) {
        if (event.status() == InventoryEvent.InventoryStatus.OUT_OF_STOCK) {
            log.info("Initiating compensating payment refund for orderId: {} due to inventory failure", event.orderId());
            PaymentEvent refundEvent = PaymentEvent.refunded(event);
            kafkaTemplate.send(KafkaConfig.PAYMENT_EVENTS_TOPIC, event.orderId().toString(), refundEvent);
        }
    }
}
