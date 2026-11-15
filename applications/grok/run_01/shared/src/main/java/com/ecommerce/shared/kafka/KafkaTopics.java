package com.ecommerce.shared.kafka;

public final class KafkaTopics {

    public static final String ORDER_EVENTS = "order-events";
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String INVENTORY_EVENTS = "inventory-events";

    public static final String ORDER_EVENTS_DLT = "order-events.DLT";
    public static final String PAYMENT_EVENTS_DLT = "payment-events.DLT";
    public static final String INVENTORY_EVENTS_DLT = "inventory-events.DLT";

    private KafkaTopics() {
    }
}
