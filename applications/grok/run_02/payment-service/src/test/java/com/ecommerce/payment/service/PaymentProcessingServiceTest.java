package com.ecommerce.payment.service;

import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.event.PaymentStatus;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.common.model.OrderItem;
import com.ecommerce.payment.producer.PaymentEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceTest {

    @Mock
    private PaymentEventProducer paymentEventProducer;

    private PaymentProcessingService service;

    @BeforeEach
    void setUp() {
        service = new PaymentProcessingService(paymentEventProducer);
    }

    @Test
    void processSuccessfulPayment() {
        OrderCreatedEvent event = order("cust-1", new BigDecimal("20.00"));

        PaymentEvent result = service.process(event);

        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.orderId()).isEqualTo(event.orderId());
        verify(paymentEventProducer).publish(result);
    }

    @Test
    void processFailsWhenCustomerMarkedFail() {
        OrderCreatedEvent event = order("cust-FAIL", new BigDecimal("20.00"));

        PaymentEvent result = service.process(event);

        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.reason()).contains("declined");
        verify(paymentEventProducer).publish(result);
    }

    @Test
    void processFailsWhenAmountExceedsLimit() {
        OrderCreatedEvent event = order("cust-1", new BigDecimal("10000.01"));

        PaymentEvent result = service.process(event);

        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.reason()).contains("limit");
    }

    @Test
    void processRejectsInvalidEvent() {
        assertThatThrownBy(() -> service.process(null))
                .isInstanceOf(ProcessingException.class);
    }

    private OrderCreatedEvent order(String customerId, BigDecimal amount) {
        return new OrderCreatedEvent(
                "evt-1",
                "order-1",
                customerId,
                List.of(new OrderItem("SKU-001", 1, amount)),
                amount,
                Instant.now()
        );
    }
}
