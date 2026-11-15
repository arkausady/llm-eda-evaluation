package com.example.orderprocessing.service;

import com.example.orderprocessing.event.KafkaTopics;
import com.example.orderprocessing.event.OrderCreatedEvent;
import com.example.orderprocessing.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    public PaymentService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "payment-service")
    public void processPayment(OrderCreatedEvent event) {
        try {
            boolean success = random.nextDouble() > 0.1; // 90% success rate
            String paymentId = UUID.randomUUID().toString();
            String status = success ? "SUCCESS" : "FAILED";
            String message = success ? "Payment processed successfully" : "Insufficient funds";

            PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(
                    event.orderId(),
                    paymentId,
                    status,
                    message,
                    LocalDateTime.now()
            );
            kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, String.valueOf(event.orderId()), paymentEvent);
            log.info("Published PaymentProcessedEvent for order {} with status {}", event.orderId(), status);
        } catch (Exception e) {
            log.error("Error processing payment for order {}: {}", event.orderId(), e.getMessage(), e);
            PaymentProcessedEvent failedEvent = new PaymentProcessedEvent(
                    event.orderId(),
                    null,
                    "FAILED",
                    "Payment processing error: " + e.getMessage(),
                    LocalDateTime.now()
            );
            try {
                kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, String.valueOf(event.orderId()), failedEvent);
                log.info("Published failure PaymentProcessedEvent for order {}", event.orderId());
            } catch (Exception ex) {
                log.error("Failed to publish payment failure event for order {}", event.orderId(), ex);
            }
        }
    }
}