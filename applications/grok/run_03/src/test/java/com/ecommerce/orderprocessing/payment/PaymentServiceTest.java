package com.ecommerce.orderprocessing.payment;

import com.ecommerce.orderprocessing.event.OrderCreatedEvent;
import com.ecommerce.orderprocessing.event.PaymentEvent;
import com.ecommerce.orderprocessing.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    void processesSuccessfulPayment() {
        PaymentEvent event = paymentService.process(order("CREDIT_CARD", new BigDecimal("10.00")));
        assertTrue(event.success());
        assertEquals(PaymentEvent.PROCESSED, event.eventType());
    }

    @Test
    void declinesConfiguredFailureMethod() {
        PaymentEvent event = paymentService.process(order("FAIL", new BigDecimal("10.00")));
        assertFalse(event.success());
        assertEquals(PaymentEvent.FAILED, event.eventType());
    }

    @Test
    void declinesNonPositiveAmount() {
        PaymentEvent event = paymentService.process(order("CREDIT_CARD", BigDecimal.ZERO));
        assertFalse(event.success());
    }

    private OrderCreatedEvent order(String method, BigDecimal amount) {
        return new OrderCreatedEvent(
                "evt-1",
                OrderCreatedEvent.TYPE,
                Instant.now(),
                "order-1",
                "CUST-1",
                "alice@example.com",
                List.of(new OrderItem("PROD-001", "Laptop", 1, amount)),
                amount,
                method
        );
    }
}
