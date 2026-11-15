package com.example.ecommerce.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InventoryRequest(@NotBlank String sku, @Min(0) int availableQuantity) {
}
