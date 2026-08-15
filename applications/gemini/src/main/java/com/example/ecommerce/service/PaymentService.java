package com.example.ecommerce.service;

import com.example.ecommerce.event.OrderEvent;
import com.example.ecommerce.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.payment-events}")
    private String paymentEventsTopic;

    public PaymentService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processPayment(OrderEvent orderEvent) {
        log.info("FR3: Processing payment for order {}", orderEvent.orderId());

        boolean isPaymentSuccessful = orderEvent.totalAmount() <= 5000.0 && !"INVALID_PAYMENT".equals(orderEvent.customerId());
        
        String paymentId = UUID.randomUUID().toString();
        String status = isPaymentSuccessful ? "SUCCESS" : "FAILED";
        String reason = isPaymentSuccessful ? "Payment processed successfully" : "Payment declined: Limit exceeded or invalid payment details";

        PaymentEvent paymentEvent = new PaymentEvent(
                paymentId,
                orderEvent.orderId(),
                orderEvent.totalAmount(),
                status,
                reason,
                System.currentTimeMillis()
        );

        kafkaTemplate.send(paymentEventsTopic, orderEvent.orderId(), paymentEvent);
        log.info("Published PaymentEvent for order {} with status {}", orderEvent.orderId(), status);
    }
}