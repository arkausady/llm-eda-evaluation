package com.ecommerce.notification.domain;

import java.time.Instant;

public class Notification {

    private final String notificationId;
    private final String orderId;
    private final String eventType;
    private final String message;
    private final Instant createdAt;

    public Notification(String notificationId, String orderId, String eventType, String message, Instant createdAt) {
        this.notificationId = notificationId;
        this.orderId = orderId;
        this.eventType = eventType;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
