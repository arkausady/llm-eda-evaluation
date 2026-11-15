package com.example.orderprocessing.event;

public final class KafkaTopics {

    public static final String ORDER_EVENTS = "order-events";
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String INVENTORY_EVENTS = "inventory-events";

    private KafkaTopics() {
    }
}