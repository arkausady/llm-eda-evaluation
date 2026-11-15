package com.ecommerce.orderprocessing.event;

import com.ecommerce.orderprocessing.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaymentEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String orderId,
        String customerId,
        String customerEmail,
        List<OrderItem> items,
        BigDecimal totalAmount,
        String paymentMethod,
        boolean success,
        String message
) {
    public static final String PROCESSED = "PAYMENT_PROCESSED";
    public static final String FAILED = "PAYMENT_FAILED";
}
