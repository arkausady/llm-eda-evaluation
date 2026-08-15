package com.ecommerce.model;

import java.time.Instant;
import java.util.UUID;

public class NotificationMessage {

    private String notificationId;
    private String orderId;
    private String customerEmail;
    private String channel;
    private String subject;
    private String body;
    private Instant createdAt;

    public NotificationMessage() {
    }

    public static NotificationMessage email(String orderId, String customerEmail, String subject, String body) {
        NotificationMessage message = new NotificationMessage();
        message.notificationId = UUID.randomUUID().toString();
        message.orderId = orderId;
        message.customerEmail = customerEmail;
        message.channel = "EMAIL";
        message.subject = subject;
        message.body = body;
        message.createdAt = Instant.now();
        return message;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
