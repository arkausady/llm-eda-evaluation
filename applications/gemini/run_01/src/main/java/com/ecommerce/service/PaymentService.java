package com.ecommerce.service;

import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.InventoryStatus;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentStatus;
import com.ecommerce.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal MAX_ALLOWED_PAYMENT = new BigDecimal("10000.00");

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String paymentEventsTopic;

    public PaymentService(KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${app.kafka.topics.payment-events}") String paymentEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentEventsTopic = paymentEventsTopic;
    }

    /**
     * FR3: Process payment from the received order-created event.
     * FR6: Handle processing failure (e.g. amount exceeds limit or invalid payment account).
     */
    @KafkaListener(topics = "${app.kafka.topics.order-events}", groupId = "payment-service-group")
    public void processPayment(OrderEvent orderEvent) {
        if (orderEvent.status() != OrderStatus.CREATED) {
            return;
        }

        log.info("PaymentService processing payment for orderId: {}, amount: {}",
                orderEvent.orderId(), orderEvent.totalAmount());

        PaymentEvent paymentEvent;

        // Simulate payment validation and failure scenarios (FR6)
        if (orderEvent.totalAmount() == null || orderEvent.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            paymentEvent = new PaymentEvent(
                    UUID.randomUUID(),
                    orderEvent.orderId(),
                    orderEvent.customerId(),
                    orderEvent.productId(),
                    orderEvent.quantity(),
                    orderEvent.totalAmount(),
                    PaymentStatus.FAILED,
                    "Invalid payment amount",
                    Instant.now()
            );
            log.warn("Payment failed for orderId {}: Invalid amount", orderEvent.orderId());
        } else if (orderEvent.totalAmount().compareTo(MAX_ALLOWED_PAYMENT) > 0
                || "FAIL_PAYMENT".equalsIgnoreCase(orderEvent.customerId())) {
            paymentEvent = new PaymentEvent(
                    UUID.randomUUID(),
                    orderEvent.orderId(),
                    orderEvent.customerId(),
                    orderEvent.productId(),
                    orderEvent.quantity(),
                    orderEvent.totalAmount(),
                    PaymentStatus.FAILED,
                    "Payment declined: limit exceeded or customer card declined",
                    Instant.now()
            );
            log.warn("Payment declined for orderId {}", orderEvent.orderId());
        } else {
            paymentEvent = new PaymentEvent(
                    UUID.randomUUID(),
                    orderEvent.orderId(),
                    orderEvent.customerId(),
                    orderEvent.productId(),
                    orderEvent.quantity(),
                    orderEvent.totalAmount(),
                    PaymentStatus.SUCCESS,
                    "Payment processed successfully",
                    Instant.now()
            );
            log.info("Payment succeeded for orderId {}", orderEvent.orderId());
        }

        kafkaTemplate.send(paymentEventsTopic, orderEvent.orderId().toString(), paymentEvent);
    }

    /**
     * FR6: Compensating transaction - refund payment if inventory reservation fails.
     */
    @KafkaListener(topics = "${app.kafka.topics.inventory-events}", groupId = "payment-service-group")
    public void handleInventoryFailureCompensation(InventoryEvent inventoryEvent) {
        if (inventoryEvent.status() == InventoryStatus.OUT_OF_STOCK) {
            log.info("PaymentService initiating compensating refund for orderId: {} due to inventory shortage",
                    inventoryEvent.orderId());

            PaymentEvent refundEvent = new PaymentEvent(
                    UUID.randomUUID(),
                    inventoryEvent.orderId(),
                    inventoryEvent.customerId(),
                    inventoryEvent.productId(),
                    inventoryEvent.quantity(),
                    BigDecimal.ZERO,
                    PaymentStatus.REFUNDED,
                    "Payment refunded due to out-of-stock product: " + inventoryEvent.productId(),
                    Instant.now()
            );

            kafkaTemplate.send(paymentEventsTopic, inventoryEvent.orderId().toString(), refundEvent);
        }
    }
}
