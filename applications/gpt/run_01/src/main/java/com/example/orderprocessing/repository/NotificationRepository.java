package com.example.orderprocessing.repository;

import com.example.orderprocessing.model.StatusNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<StatusNotification, UUID> {
    List<StatusNotification> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
