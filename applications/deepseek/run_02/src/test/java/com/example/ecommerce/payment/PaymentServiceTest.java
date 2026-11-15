package com.example.ecommerce.payment;

import com.example.ecommerce.common.EventEnvelope;
import com.example.ecommerce.order.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_shouldPublishPaymentEvent() {
        ReflectionTestUtils.setField(paymentService, "paymentEventsTopic", "payment-events");

        OrderCreatedEvent orderEvent = new OrderCreatedEvent("O1", "C1", List.of(), BigDecimal.TEN, System.currentTimeMillis());
        paymentService.processPayment(orderEvent);

        ArgumentCaptor<EventEnvelope> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(eq("payment-events"), eq("O1"), captor.capture());

        EventEnvelope envelope = captor.getValue();
        assertEquals("PaymentProcessed", envelope.getEventType());
        assertTrue(envelope.getPayload() instanceof PaymentEvent);
        PaymentEvent paymentEvent = (PaymentEvent) envelope.getPayload();
        assertEquals("O1", paymentEvent.getOrderId());
        assertTrue(paymentEvent.getPaymentStatus().equals("SUCCESS") || paymentEvent.getPaymentStatus().equals("FAILED"));
    }
}