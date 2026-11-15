package com.ecommerce.shared.event;

import com.ecommerce.shared.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        String eventId,
        String eventType,
        String orderId,
        String customerId,
        List<OrderItem> items,
        BigDecimal totalAmount,
        Instant timestamp
) {
    public static OrderCreatedEvent create(
            String orderId,
            String customerId,
            List<OrderItem> items,
            BigDecimal totalAmount) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                EventTypes.ORDER_CREATED,
                orderId,
                customerId,
                items,
                totalAmount,
                Instant.now()
        );
    }
}
