package com.example.orderprocessing.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
    boolean existsByOrderIdAndStatus(UUID orderId, String status);
}
