package com.ecommerce.shared.event;

import com.ecommerce.shared.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentEvent(
        String eventId,
        String eventType,
        String orderId,
        String customerId,
        List<OrderItem> items,
        BigDecimal amount,
        String transactionId,
        String reason,
        Instant timestamp
) {
    public static PaymentEvent completed(
            String orderId,
            String customerId,
            List<OrderItem> items,
            BigDecimal amount,
            String transactionId) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                EventTypes.PAYMENT_COMPLETED,
                orderId,
                customerId,
                items,
                amount,
                transactionId,
                null,
                Instant.now()
        );
    }

    public static PaymentEvent failed(
            String orderId,
            String customerId,
            List<OrderItem> items,
            BigDecimal amount,
            String reason) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                EventTypes.PAYMENT_FAILED,
                orderId,
                customerId,
                items,
                amount,
                null,
                reason,
                Instant.now()
        );
    }

    public static PaymentEvent refunded(
            String orderId,
            String customerId,
            BigDecimal amount,
            String originalTransactionId,
            String reason) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                EventTypes.PAYMENT_REFUNDED,
                orderId,
                customerId,
                List.of(),
                amount,
                originalTransactionId,
                reason,
                Instant.now()
        );
    }
}
