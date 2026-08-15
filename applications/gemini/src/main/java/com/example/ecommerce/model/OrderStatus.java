package com.example.ecommerce.model;

public enum OrderStatus {
    PENDING,
    PAYMENT_PROCESSING,
    PAYMENT_SUCCESSFUL,
    PAYMENT_FAILED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    COMPLETED,
    CANCELLED
}