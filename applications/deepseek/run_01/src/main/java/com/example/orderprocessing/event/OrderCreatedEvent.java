package com.example.orderprocessing.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal amount,
        LocalDateTime createdAt
) {
}