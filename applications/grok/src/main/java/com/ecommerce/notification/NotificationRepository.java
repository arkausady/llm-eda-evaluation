package com.ecommerce.notification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;

import com.ecommerce.model.NotificationMessage;

@Repository
public class NotificationRepository {

    private final CopyOnWriteArrayList<NotificationMessage> notifications = new CopyOnWriteArrayList<>();

    public NotificationMessage save(NotificationMessage message) {
        notifications.add(message);
        return message;
    }

    public List<NotificationMessage> findAll() {
        return notifications.stream()
                .sorted(Comparator.comparing(NotificationMessage::getCreatedAt).reversed())
                .toList();
    }

    public List<NotificationMessage> findByOrderId(String orderId) {
        return notifications.stream()
                .filter(message -> orderId.equals(message.getOrderId()))
                .sorted(Comparator.comparing(NotificationMessage::getCreatedAt))
                .toList();
    }

    public void clear() {
        notifications.clear();
    }

    public List<NotificationMessage> snapshot() {
        return new ArrayList<>(notifications);
    }
}
