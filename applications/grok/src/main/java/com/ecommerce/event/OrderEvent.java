package com.ecommerce.event;

import java.time.Instant;
import java.util.UUID;

import com.ecommerce.model.CustomerOrder;

public class OrderEvent {

    private String eventId;
    private OrderEventType eventType;
    private String orderId;
    private Instant timestamp;
    private CustomerOrder order;
    private String reason;

    public OrderEvent() {
    }

    public static OrderEvent of(OrderEventType type, CustomerOrder order, String reason) {
        OrderEvent event = new OrderEvent();
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

    public OrderEventType getEventType() {
        return eventType;
    }

    public void setEventType(OrderEventType eventType) {
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
