package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
