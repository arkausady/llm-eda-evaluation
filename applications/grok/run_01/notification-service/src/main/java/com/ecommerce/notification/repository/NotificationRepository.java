package com.ecommerce.notification.repository;

import com.ecommerce.notification.domain.Notification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class NotificationRepository {

    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public void save(Notification notification) {
        notifications.add(notification);
    }

    public List<Notification> findAll() {
        return new ArrayList<>(notifications);
    }

    public List<Notification> findByOrderId(String orderId) {
        return notifications.stream()
                .filter(n -> n.getOrderId().equals(orderId))
                .toList();
    }
}
