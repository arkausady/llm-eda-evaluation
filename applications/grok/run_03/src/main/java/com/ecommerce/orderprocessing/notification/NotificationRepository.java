package com.ecommerce.orderprocessing.notification;

import com.ecommerce.orderprocessing.model.OrderNotification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class NotificationRepository {

    private final CopyOnWriteArrayList<OrderNotification> notifications = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, List<OrderNotification>> byOrder = new ConcurrentHashMap<>();

    public OrderNotification save(OrderNotification notification) {
        notifications.add(notification);
        byOrder.computeIfAbsent(notification.orderId(), key -> new CopyOnWriteArrayList<>()).add(notification);
        return notification;
    }

    public List<OrderNotification> findAll() {
        return List.copyOf(notifications);
    }

    public List<OrderNotification> findByOrderId(String orderId) {
        return List.copyOf(byOrder.getOrDefault(orderId, List.of()));
    }
}
