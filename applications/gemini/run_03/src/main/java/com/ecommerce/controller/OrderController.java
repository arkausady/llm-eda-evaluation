package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.NotificationLog;
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
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/orders/{orderId}/notifications")
    public ResponseEntity<List<NotificationLog>> getOrderNotifications(@PathVariable UUID orderId) {
        return ResponseEntity.ok(notificationService.getNotificationsForOrder(orderId));
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryItem>> getInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PutMapping("/inventory/{productId}")
    public ResponseEntity<InventoryItem> updateInventory(@PathVariable String productId, @RequestParam int quantity) {
        return ResponseEntity.ok(inventoryService.addOrUpdateInventory(productId, quantity));
    }
}
