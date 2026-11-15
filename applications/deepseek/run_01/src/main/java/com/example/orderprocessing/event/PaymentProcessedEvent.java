package com.example.orderprocessing.event;

import java.time.LocalDateTime;

public record PaymentProcessedEvent(
        Long orderId,
        String paymentId,
        String status,
        String message,
        LocalDateTime processedAt
) {
}