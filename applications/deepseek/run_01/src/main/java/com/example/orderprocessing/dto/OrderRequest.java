package com.example.orderprocessing.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank String customerId,
        @NotBlank String productId,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}