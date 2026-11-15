package com.example.orderprocessing.model;

public enum OrderStatus {
    PENDING,
    PAYMENT_PROCESSED,
    COMPLETED,
    PAYMENT_FAILED,
    INVENTORY_FAILED,
    EVENT_PUBLICATION_FAILED
}
