package com.example.orderprocessing.event;

public enum EventType {
    ORDER_CREATED,
    PAYMENT_SUCCEEDED,
    PAYMENT_FAILED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    ORDER_COMPLETED,
    ORDER_FAILED
}
