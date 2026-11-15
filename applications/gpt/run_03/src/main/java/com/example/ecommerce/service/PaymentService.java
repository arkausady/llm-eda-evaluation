package com.example.ecommerce.service;

import com.example.ecommerce.model.EventType;
import com.example.ecommerce.model.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final EventPublisher eventPublisher;

    public PaymentService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void processPayment(OrderEvent event) {
        if (event.type() != EventType.ORDER_CREATED) {
            return;
        }

        if (event.amount() == null || event.amount().signum() <= 0) {
            eventPublisher.publish("payment-events",
                    event.withType(EventType.PAYMENT_FAILED, "Payment amount must be positive"));
            return;
        }

        if (event.paymentMethodId() == null
                || event.paymentMethodId().toUpperCase().startsWith("DECLINE")) {
            eventPublisher.publish("payment-events",
                    event.withType(EventType.PAYMENT_FAILED, "Payment was declined"));
            return;
        }

        eventPublisher.publish("payment-events", event.withType(EventType.PAYMENT_COMPLETED, null));
    }
}
