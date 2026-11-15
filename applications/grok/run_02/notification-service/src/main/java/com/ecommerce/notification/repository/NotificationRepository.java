package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class NotificationRepository {

    private final CopyOnWriteArrayList<Notification> store = new CopyOnWriteArrayList<>();

    public Notification save(Notification notification) {
        store.add(notification);
        return notification;
    }

    public List<Notification> findAll() {
        return new ArrayList<>(store);
    }

    public List<Notification> findByOrderId(String orderId) {
        return store.stream().filter(notification -> notification.orderId().equals(orderId)).toList();
    }
}
