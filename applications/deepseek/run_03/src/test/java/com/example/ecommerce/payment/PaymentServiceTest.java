package com.example.ecommerce.payment;

import com.example.ecommerce.model.OrderCreatedEvent;
import com.example.ecommerce.model.PaymentProcessedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentSuccessWhenAmountBelowLimit() {
        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
                "order1", "cust1", "prod1", 1, new BigDecimal("500.00")
        );

        paymentService.processPayment(orderEvent);

        ArgumentCaptor<PaymentProcessedEvent> captor = ArgumentCaptor.forClass(PaymentProcessedEvent.class);
        verify(paymentEventPublisher, times(1)).publishPaymentProcessed(captor.capture());
        PaymentProcessedEvent event = captor.getValue();
        assertEquals("SUCCESS", event.getStatus());
        assertEquals("Payment processed successfully", event.getMessage());
    }

    @Test
    void processPaymentFailureWhenAmountExceedsLimit() {
        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
                "order2", "cust1", "prod1", 1, new BigDecimal("1500.00")
        );

        paymentService.processPayment(orderEvent);

        ArgumentCaptor<PaymentProcessedEvent> captor = ArgumentCaptor.forClass(PaymentProcessedEvent.class);
        verify(paymentEventPublisher, times(1)).publishPaymentProcessed(captor.capture());
        PaymentProcessedEvent event = captor.getValue();
        assertEquals("FAILED", event.getStatus());
        assertEquals("Payment failed: amount exceeds limit", event.getMessage());
    }
}
