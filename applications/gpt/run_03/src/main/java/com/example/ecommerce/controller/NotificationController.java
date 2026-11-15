package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Notification;
import com.example.ecommerce.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/orders/{orderId}")
    public List<Notification> findByOrder(@PathVariable UUID orderId) {
        return notificationService.findByOrderId(orderId);
    }
}
