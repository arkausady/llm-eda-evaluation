package com.ecommerce.dto;

import com.ecommerce.model.OrderEntity;
import com.ecommerce.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderResponse fromEntity(OrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
