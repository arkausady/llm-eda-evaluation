package com.example.ecommerce.dto;

import com.example.ecommerce.model.OrderStatus;

public record OrderResponse(
    String orderId,
    String customerId,
    String productId,
    int quantity,
    double totalAmount,
    OrderStatus status,
    String failureReason
) {}