package com.example.ecommerce.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank @Email String customerEmail,
        @NotBlank String sku,
        @Min(1) int quantity,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice,
        @NotBlank String paymentMethodId
) {
}
