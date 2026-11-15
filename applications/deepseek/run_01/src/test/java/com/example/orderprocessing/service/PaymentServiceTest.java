package com.example.orderprocessing.service;

import com.example.orderprocessing.event.KafkaTopics;
import com.example.orderprocessing.event.OrderCreatedEvent;
import com.example.orderprocessing.event.PaymentProcessedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentShouldPublishEvent() {
        OrderCreatedEvent event = new OrderCreatedEvent(1L, "cust1", "prod1", 2, new BigDecimal("100.00"), LocalDateTime.now());

        paymentService.processPayment(event);

        verify(kafkaTemplate, times(1)).send(eq(KafkaTopics.PAYMENT_EVENTS), eq("1"), any());
    }
}