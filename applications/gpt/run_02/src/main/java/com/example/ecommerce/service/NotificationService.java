package com.example.ecommerce.service;

import com.example.ecommerce.config.KafkaTopics;
import com.example.ecommerce.event.InventoryEvent;
import com.example.ecommerce.event.InventoryStatus;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.event.PaymentStatus;
import com.example.ecommerce.model.OrderNotification;
import com.example.ecommerce.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "notification-payment-service")
    public void notifyPaymentFailure(PaymentEvent event) {
        if (event.status() == PaymentStatus.FAILED) {
            saveNotification(event.eventId(), event.orderId(), "FAILED",
                    "Order payment failed: " + event.reason());
        }
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "notification-inventory-service")
    public void notifyInventoryResult(InventoryEvent event) {
        String status = event.status() == InventoryStatus.UPDATED ? "COMPLETED" : "FAILED";
        String message = event.status() == InventoryStatus.UPDATED
                ? "Order processing completed successfully"
                : "Order inventory update failed: " + event.reason();
        saveNotification(event.eventId(), event.orderId(), status, message);
    }

    public List<OrderNotification> getNotifications(String orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    private void saveNotification(String eventId, String orderId, String status, String message) {
        if (notificationRepository.existsByEventId(eventId)) {
            return;
        }
        notificationRepository.save(new OrderNotification(eventId, orderId, status, message, Instant.now()));
        log.info("Order notification: orderId={}, status={}, message={}", orderId, status, message);
    }
}
