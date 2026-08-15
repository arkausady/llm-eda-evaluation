package com.example.ecommerce.dto;

public record CreateOrderRequest(
    String customerId,
    String productId,
    int quantity,
    double price
) {}