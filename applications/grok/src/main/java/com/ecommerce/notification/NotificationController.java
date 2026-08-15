package com.ecommerce.notification;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.model.NotificationMessage;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationMessage> all() {
        return notificationService.getAll();
    }

    @GetMapping("/order/{orderId}")
    public List<NotificationMessage> byOrder(@PathVariable String orderId) {
        return notificationService.getByOrderId(orderId);
    }
}
