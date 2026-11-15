package com.ecommerce.orderprocessing.event;

import com.ecommerce.orderprocessing.model.CustomerOrder;
import com.ecommerce.orderprocessing.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        String orderId,
        String customerId,
        String customerEmail,
        List<OrderItem> items,
        BigDecimal totalAmount,
        String paymentMethod
) {
    public static final String TYPE = "ORDER_CREATED";

    public static OrderCreatedEvent from(CustomerOrder order) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                TYPE,
                Instant.now(),
                order.orderId(),
                order.customerId(),
                order.customerEmail(),
                order.items(),
                order.totalAmount(),
                order.paymentMethod()
        );
    }
}
