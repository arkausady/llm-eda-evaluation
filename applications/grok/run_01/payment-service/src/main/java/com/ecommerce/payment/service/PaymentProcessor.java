package com.ecommerce.payment.service;

import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.event.PaymentEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentProcessor {

    static final BigDecimal PAYMENT_LIMIT = new BigDecimal("10000");

    public PaymentEvent process(OrderCreatedEvent orderEvent) {
        if (orderEvent.customerId() != null && orderEvent.customerId().toLowerCase().contains("crash")) {
            throw new IllegalStateException("Simulated technical payment failure");
        }

        if (orderEvent.customerId() != null && orderEvent.customerId().toLowerCase().contains("fail")) {
            return PaymentEvent.failed(
                    orderEvent.orderId(),
                    orderEvent.customerId(),
                    orderEvent.items(),
                    orderEvent.totalAmount(),
                    "Payment method declined"
            );
        }

        if (orderEvent.totalAmount() != null && orderEvent.totalAmount().compareTo(PAYMENT_LIMIT) > 0) {
            return PaymentEvent.failed(
                    orderEvent.orderId(),
                    orderEvent.customerId(),
                    orderEvent.items(),
                    orderEvent.totalAmount(),
                    "Amount exceeds payment limit of " + PAYMENT_LIMIT
            );
        }

        return PaymentEvent.completed(
                orderEvent.orderId(),
                orderEvent.customerId(),
                orderEvent.items(),
                orderEvent.totalAmount(),
                "txn-" + UUID.randomUUID()
        );
    }
}
