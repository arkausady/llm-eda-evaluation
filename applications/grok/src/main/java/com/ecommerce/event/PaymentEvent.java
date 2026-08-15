package com.ecommerce.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.PaymentRecord;

public class PaymentEvent {

    private String eventId;
    private PaymentEventType eventType;
    private String orderId;
    private String paymentId;
    private BigDecimal amount;
    private Instant timestamp;
    private CustomerOrder order;
    private String reason;

    public PaymentEvent() {
    }

    public static PaymentEvent from(PaymentEventType type, PaymentRecord payment, CustomerOrder order, String reason) {
        PaymentEvent event = new PaymentEvent();
        event.eventId = UUID.randomUUID().toString();
        event.eventType = type;
        event.orderId = payment.getOrderId();
        event.paymentId = payment.getPaymentId();
        event.amount = payment.getAmount();
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

    public PaymentEventType getEventType() {
        return eventType;
    }

    public void setEventType(PaymentEventType eventType) {
        this.eventType = eventType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
