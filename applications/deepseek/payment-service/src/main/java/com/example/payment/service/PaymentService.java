package com.example.payment.service;

import com.example.common.event.OrderCreatedEvent;
import com.example.common.event.PaymentFailedEvent;
import com.example.common.event.PaymentProcessedEvent;
import com.example.payment.kafka.PaymentEventPublisher;
import com.example.payment.model.Payment;
import com.example.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    public void processPayment(OrderCreatedEvent orderCreated) {
        // Simulate payment processing (80% success)
        boolean success = Math.random() < 0.8;
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderCreated.getOrderId());
        payment.setSuccess(success);
        payment.setCreatedAt(Instant.now());
        paymentRepository.save(payment);

        if (success) {
            PaymentProcessedEvent event = new PaymentProcessedEvent(
                orderCreated.getOrderId(),
                payment.getId(),
                true,
                payment.getCreatedAt()
            );
            eventPublisher.publishPaymentProcessed(event);
        } else {
            PaymentFailedEvent event = new PaymentFailedEvent(
                orderCreated.getOrderId(),
                "Payment declined",
                payment.getCreatedAt()
            );
            eventPublisher.publishPaymentFailed(event);
        }
    }
}
