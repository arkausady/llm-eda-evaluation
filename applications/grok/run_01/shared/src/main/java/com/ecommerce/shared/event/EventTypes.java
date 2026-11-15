package com.ecommerce.shared.event;

public final class EventTypes {

    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PAYMENT_REFUNDED = "PAYMENT_REFUNDED";
    public static final String INVENTORY_RESERVED = "INVENTORY_RESERVED";
    public static final String INVENTORY_FAILED = "INVENTORY_FAILED";

    private EventTypes() {
    }
}
