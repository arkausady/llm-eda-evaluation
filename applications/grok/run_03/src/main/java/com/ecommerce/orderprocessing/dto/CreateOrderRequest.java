package com.ecommerce.orderprocessing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotBlank @Email String customerEmail,
        @NotBlank String paymentMethod,
        @NotEmpty @Valid List<OrderItemRequest> items
) {
}
