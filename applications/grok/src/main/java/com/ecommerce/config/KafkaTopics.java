package com.ecommerce.config;

public final class KafkaTopics {

    public static final String ORDER_EVENTS = "order-events";
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String INVENTORY_EVENTS = "inventory-events";

    public static final String ORDER_EVENTS_DLT = ORDER_EVENTS + ".DLT";
    public static final String PAYMENT_EVENTS_DLT = PAYMENT_EVENTS + ".DLT";
    public static final String INVENTORY_EVENTS_DLT = INVENTORY_EVENTS + ".DLT";

    public static final String PAYMENT_SERVICE_GROUP = "payment-service-group";
    public static final String INVENTORY_SERVICE_GROUP = "inventory-service-group";
    public static final String ORDER_SERVICE_GROUP = "order-service-group";
    public static final String NOTIFICATION_SERVICE_GROUP = "notification-service-group";
    public static final String DLT_HANDLER_GROUP = "dlt-handler-group";

    private KafkaTopics() {
    }
}
