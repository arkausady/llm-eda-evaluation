package com.example.ecommerce.service;

import com.example.ecommerce.entity.Notification;
import com.example.ecommerce.model.EventType;
import com.example.ecommerce.model.OrderEvent;
import com.example.ecommerce.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = {"payment-events", "inventory-events"}, groupId = "notification-service")
    @Transactional
    public void generateNotification(OrderEvent event) {
        String message = switch (event.type()) {
            case PAYMENT_COMPLETED -> "Payment completed for order " + event.orderId();
            case PAYMENT_FAILED -> "Payment failed for order " + event.orderId() + ": " + event.reason();
            case INVENTORY_RESERVED -> "Order " + event.orderId() + " completed and inventory reserved";
            case INVENTORY_FAILED -> "Order " + event.orderId() + " failed: " + event.reason();
            default -> null;
        };

        if (message == null) {
            return;
        }

        Notification notification = new Notification(event.orderId(), event.customerEmail(),
                event.type().name(), message);
        notificationRepository.save(notification);
        log.info("Notification for {}: {}", event.customerEmail(), message);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByOrderId(UUID orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}
