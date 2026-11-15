package com.example.orderprocessing.service;

import com.example.orderprocessing.event.OrderCreatedEvent;
import com.example.orderprocessing.event.OrderItemData;
import com.example.orderprocessing.event.PaymentEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PaymentServiceTest {
    @Test
    void publishesSuccessfulPayment() {
        var publisher = mock(EventPublisher.class);
        var service = new PaymentService(publisher);
        var orderId = UUID.randomUUID();
        var event = event(orderId, "valid-token", new BigDecimal("20.00"));

        service.process(event);

        var captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publish(org.mockito.ArgumentMatchers.eq("payment-events"),
                org.mockito.ArgumentMatchers.eq(orderId.toString()), captor.capture());
        var payment = (PaymentEvent) captor.getValue();
        assertEquals(PaymentEvent.PaymentStatus.SUCCEEDED, payment.status());
    }

    @Test
    void publishesFailedPaymentForDeclinedToken() {
        var publisher = mock(EventPublisher.class);
        var service = new PaymentService(publisher);
        var orderId = UUID.randomUUID();

        service.process(event(orderId, "FAIL-card", new BigDecimal("20.00")));

        var captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publish(org.mockito.ArgumentMatchers.eq("payment-events"),
                org.mockito.ArgumentMatchers.eq(orderId.toString()), captor.capture());
        assertEquals(PaymentEvent.PaymentStatus.FAILED, ((PaymentEvent) captor.getValue()).status());
    }

    private OrderCreatedEvent event(UUID orderId, String token, BigDecimal amount) {
        return new OrderCreatedEvent(UUID.randomUUID(), orderId, "customer-1", "a@example.com",
                token, amount, List.of(new OrderItemData("product-1", 1, amount)), Instant.now());
    }
}
