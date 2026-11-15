package com.example.ecommerce.payment;

import com.example.ecommerce.model.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private final PaymentService paymentService;

    public PaymentConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        paymentService.processPayment(event);
    }
}
