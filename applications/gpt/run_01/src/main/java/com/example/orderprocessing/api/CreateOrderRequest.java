package com.example.orderprocessing.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotBlank @Email String customerEmail,
        @NotBlank String paymentToken,
        @NotEmpty List<@Valid Item> items) {

    public record Item(
            @NotBlank String productId,
            @Positive int quantity,
            @NotNull @Positive BigDecimal unitPrice) {
    }
}
