package com.example.ecommerce.service;

import com.example.ecommerce.config.KafkaTopics;
import com.example.ecommerce.event.OrderCreatedEvent;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.event.PaymentStatus;
import com.example.ecommerce.messaging.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private EventPublisher publisher;

    @Test
    void publishesFailedPaymentWhenDeclined() {
        when(publisher.publish(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        PaymentService service = new PaymentService(publisher);
        OrderCreatedEvent order = new OrderCreatedEvent("event-1", "order-1", "customer-1",
                "product-1", 1, new BigDecimal("10.00"), "DECLINE", Instant.now());

        service.processPayment(order);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publish(org.mockito.ArgumentMatchers.eq(KafkaTopics.PAYMENT_EVENTS),
                org.mockito.ArgumentMatchers.eq("order-1"), captor.capture());
        PaymentEvent paymentEvent = (PaymentEvent) captor.getValue();
        assertEquals(PaymentStatus.FAILED, paymentEvent.status());
        assertEquals("Payment was declined", paymentEvent.reason());
    }
}
