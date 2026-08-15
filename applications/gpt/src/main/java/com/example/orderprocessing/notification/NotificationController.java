package com.example.orderprocessing.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/notifications")
public class NotificationController {
    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<NotificationResponse> find(@PathVariable UUID orderId) {
        return repository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(notification -> new NotificationResponse(notification.getId(),
                        notification.getOrderId(), notification.getStatus(),
                        notification.getMessage(), notification.getCreatedAt()))
                .toList();
    }

    public record NotificationResponse(Long id, UUID orderId, String status,
                                       String message, Instant createdAt) {
    }
}
