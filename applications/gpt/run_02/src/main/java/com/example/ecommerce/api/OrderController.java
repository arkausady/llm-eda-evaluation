package com.example.ecommerce.api;

import com.example.ecommerce.model.OrderNotification;
import com.example.ecommerce.service.NotificationService;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final NotificationService notificationService;

    public OrderController(OrderService orderService, NotificationService notificationService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = OrderResponse.from(orderService.createOrder(request));
        return ResponseEntity.accepted()
                .location(URI.create("/api/orders/" + response.id()))
                .body(response);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return OrderResponse.from(orderService.getOrder(orderId));
    }

    @GetMapping
    public List<OrderResponse> getOrders() {
        return orderService.getOrders().stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{orderId}/notifications")
    public List<OrderNotification> getNotifications(@PathVariable String orderId) {
        orderService.getOrder(orderId);
        return notificationService.getNotifications(orderId);
    }
}
