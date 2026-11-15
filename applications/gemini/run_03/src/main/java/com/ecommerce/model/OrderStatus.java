package com.ecommerce.model;

public enum OrderStatus {
    CREATED,
    PAYMENT_PROCESSED,
    PAYMENT_FAILED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    COMPLETED,
    FAILED
}
