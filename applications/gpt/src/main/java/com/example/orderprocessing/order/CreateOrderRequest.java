package com.example.orderprocessing.order;

import com.example.orderprocessing.model.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotEmpty List<@Valid OrderItem> items,
        @NotBlank String paymentToken) {
}
