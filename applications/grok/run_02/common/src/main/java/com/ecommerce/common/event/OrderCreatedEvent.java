package com.ecommerce.common.event;

import com.ecommerce.common.model.Order;
import com.ecommerce.common.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        String eventId,
        String orderId,
        String customerId,
        List<OrderItem> items,
        BigDecimal totalAmount,
        Instant occurredAt
) {
    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getOrderId(),
                order.getCustomerId(),
                order.getItems(),
                order.getTotalAmount(),
                Instant.now()
        );
    }
}
