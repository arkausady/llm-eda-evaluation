package com.example.ecommerce.repository;

import com.example.ecommerce.model.OrderNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<OrderNotification, Long> {
    boolean existsByEventId(String eventId);
    List<OrderNotification> findByOrderIdOrderByCreatedAtAsc(String orderId);
}
