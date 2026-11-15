package com.ecommerce.payment.service;

import com.ecommerce.shared.event.EventTypes;
import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.event.PaymentEvent;
import com.ecommerce.shared.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentProcessorTest {

    private final PaymentProcessor processor = new PaymentProcessor();

    @Test
    void successfulPayment() {
        PaymentEvent event = processor.process(order("cust-1", new BigDecimal("100.00")));
        assertEquals(EventTypes.PAYMENT_COMPLETED, event.eventType());
        assertNotNull(event.transactionId());
    }

    @Test
    void declinedWhenCustomerContainsFail() {
        PaymentEvent event = processor.process(order("fail-user", new BigDecimal("10.00")));
        assertEquals(EventTypes.PAYMENT_FAILED, event.eventType());
        assertEquals("Payment method declined", event.reason());
    }

    @Test
    void declinedWhenAmountExceedsLimit() {
        PaymentEvent event = processor.process(order("cust-1", new BigDecimal("10000.01")));
        assertEquals(EventTypes.PAYMENT_FAILED, event.eventType());
    }

    @Test
    void technicalFailureWhenCustomerContainsCrash() {
        assertThrows(IllegalStateException.class, () -> processor.process(order("crash-user", new BigDecimal("10.00"))));
    }

    private OrderCreatedEvent order(String customerId, BigDecimal amount) {
        return new OrderCreatedEvent(
                "evt-1",
                EventTypes.ORDER_CREATED,
                "order-1",
                customerId,
                List.of(new OrderItem("PROD-1", "Item", 1, amount)),
                amount,
                Instant.parse("2024-01-01T00:00:00Z")
        );
    }
}
