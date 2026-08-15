package com.example.ecommerce.event;

public record OrderEvent(
    String orderId,
    String customerId,
    String productId,
    int quantity,
    double totalAmount,
    String status,
    String message,
    long timestamp
) {}