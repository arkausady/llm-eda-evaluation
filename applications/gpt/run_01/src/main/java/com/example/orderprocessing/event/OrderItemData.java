package com.example.orderprocessing.event;

import java.math.BigDecimal;

public record OrderItemData(String productId, int quantity, BigDecimal unitPrice) {
}
