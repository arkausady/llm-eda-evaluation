package com.example.ecommerce.event;

public record PaymentEvent(
    String paymentId,
    String orderId,
    double amount,
    String status,
    String reason,
    long timestamp
) {}