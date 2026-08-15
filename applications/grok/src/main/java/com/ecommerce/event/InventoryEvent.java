package com.ecommerce.event;

import java.time.Instant;
import java.util.UUID;

import com.ecommerce.model.CustomerOrder;

public class InventoryEvent {

    private String eventId;
    private InventoryEventType eventType;
    private String orderId;
    private Instant timestamp;
    private CustomerOrder order;
    private String reason;

    public InventoryEvent() {
    }

    public static InventoryEvent of(InventoryEventType type, CustomerOrder order, String reason) {
        InventoryEvent event = new InventoryEvent();
        event.eventId = UUID.randomUUID().toString();
        event.eventType = type;
        event.orderId = order.getOrderId();
        event.timestamp = Instant.now();
        event.order = order;
        event.reason = reason;
        return event;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public InventoryEventType getEventType() {
        return eventType;
    }

    public void setEventType(InventoryEventType eventType) {
        this.eventType = eventType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public CustomerOrder getOrder() {
        return order;
    }

    public void setOrder(CustomerOrder order) {
        this.order = order;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
