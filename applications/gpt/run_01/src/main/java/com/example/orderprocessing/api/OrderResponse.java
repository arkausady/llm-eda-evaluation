package com.example.orderprocessing.api;

import com.example.orderprocessing.model.CustomerOrder;
import com.example.orderprocessing.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        String customerEmail,
        BigDecimal totalAmount,
        OrderStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt,
        List<Item> items) {

    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getCustomerEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getFailureReason(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream()
                        .map(item -> new Item(item.getProductId(), item.getQuantity(), item.getUnitPrice()))
                        .toList());
    }

    public record Item(String productId, int quantity, BigDecimal unitPrice) {
    }
}
