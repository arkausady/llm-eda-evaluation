package com.ecommerce.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal amount,
        PaymentStatus status,
        String reason,
        Instant timestamp
) {
    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        REFUNDED
    }

    public static PaymentEvent success(OrderEvent orderEvent) {
        return new PaymentEvent(
                UUID.randomUUID(),
                orderEvent.orderId(),
                orderEvent.customerId(),
                orderEvent.productId(),
                orderEvent.quantity(),
                orderEvent.totalAmount(),
                PaymentStatus.SUCCESS,
                "Payment processed successfully",
                Instant.now()
        );
    }

    public static PaymentEvent failed(OrderEvent orderEvent, String reason) {
        return new PaymentEvent(
                UUID.randomUUID(),
                orderEvent.orderId(),
                orderEvent.customerId(),
                orderEvent.productId(),
                orderEvent.quantity(),
                orderEvent.totalAmount(),
                PaymentStatus.FAILED,
                reason,
                Instant.now()
        );
    }

    public static PaymentEvent refunded(InventoryEvent inventoryEvent) {
        return new PaymentEvent(
                UUID.randomUUID(),
                inventoryEvent.orderId(),
                inventoryEvent.customerId(),
                inventoryEvent.productId(),
                inventoryEvent.quantity(),
                BigDecimal.ZERO,
                PaymentStatus.REFUNDED,
                "Payment refunded due to inventory failure: " + inventoryEvent.reason(),
                Instant.now()
        );
    }
}
