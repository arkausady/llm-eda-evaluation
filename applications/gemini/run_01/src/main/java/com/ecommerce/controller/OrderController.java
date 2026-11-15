package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.NotificationLog;
import com.ecommerce.model.Order;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.NotificationService;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    public OrderController(OrderService orderService,
                           InventoryService inventoryService,
                           NotificationService notificationService) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.fromEntity(createdOrder));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return orderService.getOrder(orderId)
                .map(OrderResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders().stream()
                .map(OrderResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{orderId}/notifications")
    public ResponseEntity<List<NotificationLog>> getOrderNotifications(@PathVariable UUID orderId) {
        return ResponseEntity.ok(notificationService.getNotificationsForOrder(orderId));
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryItem>> getInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
}
