package com.example.orderprocessing.service;

import com.example.orderprocessing.event.InventoryEvent;
import com.example.orderprocessing.event.PaymentEvent;
import com.example.orderprocessing.exception.OrderNotFoundException;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.model.StatusNotification;
import com.example.orderprocessing.repository.NotificationRepository;
import com.example.orderprocessing.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderStatusNotificationService {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusNotificationService.class);

    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;

    public OrderStatusNotificationService(OrderRepository orderRepository,
                                          NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    @KafkaListener(topics = "payment-events", groupId = "order-notification-payment-service")
    public void handlePayment(PaymentEvent event) {
        var order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));

        if (event.status() == PaymentEvent.PaymentStatus.FAILED) {
            if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
                return;
            }
            order.changeStatus(OrderStatus.PAYMENT_FAILED, event.message());
        } else {
            if (order.getStatus() != OrderStatus.PENDING) {
                return;
            }
            order.changeStatus(OrderStatus.PAYMENT_PROCESSED, null);
        }
        orderRepository.save(order);
        createNotification(order.getId(), order.getCustomerEmail(), order.getStatus().name(), event.message());
    }

    @Transactional
    @KafkaListener(topics = "inventory-events", groupId = "order-notification-inventory-service")
    public void handleInventory(InventoryEvent event) {
        var order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));
        var newStatus = event.status() == InventoryEvent.InventoryStatus.RESERVED
                ? OrderStatus.COMPLETED
                : OrderStatus.INVENTORY_FAILED;
        if (order.getStatus() == newStatus) {
            return;
        }
        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            return;
        }
        order.changeStatus(newStatus,
                newStatus == OrderStatus.INVENTORY_FAILED ? event.message() : null);
        orderRepository.save(order);
        createNotification(order.getId(), order.getCustomerEmail(), newStatus.name(), event.message());
    }

    private void createNotification(java.util.UUID orderId, String email, String status, String message) {
        notificationRepository.save(new StatusNotification(orderId, email, status, message));
        log.info("Generated order notification: order={}, recipient={}, status={}, message={}",
                orderId, email, status, message);
    }
}
