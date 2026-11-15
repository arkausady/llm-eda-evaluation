package com.ecommerce.orderprocessing.notification;

import com.ecommerce.orderprocessing.model.OrderNotification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<OrderNotification> getAll() {
        return notificationService.getAll();
    }

    @GetMapping("/order/{orderId}")
    public List<OrderNotification> getByOrder(@PathVariable String orderId) {
        return notificationService.getByOrderId(orderId);
    }
}
