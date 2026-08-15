package com.example.orderprocessing.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderItem(
        @NotBlank String sku,
        @Min(1) int quantity) {
}
