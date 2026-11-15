package com.ecommerce.orderprocessing.dto;

import com.ecommerce.orderprocessing.model.CustomerOrder;
import com.ecommerce.orderprocessing.model.OrderItem;
import com.ecommerce.orderprocessing.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderId,
        String customerId,
        String customerEmail,
        List<OrderItem> items,
        BigDecimal totalAmount,
        String paymentMethod,
        OrderStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.orderId(),
                order.customerId(),
                order.customerEmail(),
                order.items(),
                order.totalAmount(),
                order.paymentMethod(),
                order.status(),
                order.failureReason(),
                order.createdAt(),
                order.updatedAt()
        );
    }
}
