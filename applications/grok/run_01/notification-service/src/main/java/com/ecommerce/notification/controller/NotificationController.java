package com.ecommerce.notification.controller;

import com.ecommerce.notification.domain.Notification;
import com.ecommerce.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> list(@RequestParam(required = false) String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return notificationService.findAll();
        }
        return notificationService.findByOrderId(orderId);
    }
}
