package com.example.orderprocessing.controller;

import com.example.orderprocessing.model.StatusNotification;
import com.example.orderprocessing.repository.NotificationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<StatusNotification> findByOrder(@PathVariable UUID orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}
