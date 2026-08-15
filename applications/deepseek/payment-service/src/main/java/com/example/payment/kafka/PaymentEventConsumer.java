package com.example.payment.kafka;

import com.example.common.event.OrderCreatedEvent;
import com.example.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    public PaymentEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        paymentService.processPayment(event);
    }
}
