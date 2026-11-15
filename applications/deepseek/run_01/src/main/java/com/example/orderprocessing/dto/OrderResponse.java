package com.example.orderprocessing.dto;

public record OrderResponse(Long orderId, String status, String message) {
}