package com.ecommerce.orderprocessing.model;

import java.time.Instant;

public record OrderNotification(
        String notificationId,
        String orderId,
        String customerEmail,
        String eventType,
        String message,
        Instant createdAt
) {
}
