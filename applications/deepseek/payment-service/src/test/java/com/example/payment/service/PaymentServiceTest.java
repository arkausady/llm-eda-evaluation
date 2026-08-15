package com.example.payment.service;

import com.example.common.event.OrderCreatedEvent;
import com.example.payment.kafka.PaymentEventPublisher;
import com.example.payment.model.Payment;
import com.example.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(paymentRepository, eventPublisher);
    }

    @Test
    void processPayment_success_publishesProcessedEvent() {
        OrderCreatedEvent orderCreated = new OrderCreatedEvent(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1, 10.0, Instant.now()
        );
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderCreated.getOrderId());
        payment.setSuccess(true);

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        paymentService.processPayment(orderCreated);

        verify(eventPublisher).publishPaymentProcessed(any());
        verify(eventPublisher, never()).publishPaymentFailed(any());
    }
}
