package com.example.ecommerce.payment;

import com.example.ecommerce.common.EventEnvelope;
import com.example.ecommerce.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    @Value("${kafka.topics.payment-events}")
    private String paymentEventsTopic;

    @KafkaListener(topics = "${kafka.topics.order-events}", groupId = "payment-group")
    public void onOrderCreated(EventEnvelope<OrderCreatedEvent> envelope) {
        OrderCreatedEvent orderEvent = envelope.getPayload();
        processPayment(orderEvent);
    }

    public void processPayment(OrderCreatedEvent orderEvent) {
        // Simulate payment processing: 90% success rate
        boolean success = random.nextDouble() < 0.9;
        String status = success ? "SUCCESS" : "FAILED";
        String transactionId = UUID.randomUUID().toString();

        PaymentEvent paymentEvent = new PaymentEvent(orderEvent.getOrderId(), status, transactionId, System.currentTimeMillis());
        EventEnvelope<PaymentEvent> paymentEnvelope = EventEnvelope.of("PaymentProcessed", paymentEvent);
        kafkaTemplate.send(paymentEventsTopic, orderEvent.getOrderId(), paymentEnvelope);
    }
}