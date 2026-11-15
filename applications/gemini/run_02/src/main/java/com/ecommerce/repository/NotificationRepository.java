package com.ecommerce.repository;

import com.ecommerce.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByOrderIdOrderByTimestampAsc(UUID orderId);
}