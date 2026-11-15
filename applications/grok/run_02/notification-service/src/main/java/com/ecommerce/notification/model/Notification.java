package com.ecommerce.notification.model;

import java.time.Instant;

public record Notification(
        String id,
        String orderId,
        String type,
        String message,
        Instant createdAt
) {
}
