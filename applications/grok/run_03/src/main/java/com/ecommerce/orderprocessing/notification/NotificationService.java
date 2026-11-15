package com.ecommerce.orderprocessing.notification;

import com.ecommerce.orderprocessing.model.OrderNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public OrderNotification notify(String orderId, String customerEmail, String eventType, String message) {
        OrderNotification notification = new OrderNotification(
                UUID.randomUUID().toString(),
                orderId,
                customerEmail,
                eventType,
                message,
                Instant.now()
        );
        notificationRepository.save(notification);
        log.info("Notification {} for order {}: {}", eventType, orderId, message);
        return notification;
    }

    public List<OrderNotification> getAll() {
        return notificationRepository.findAll();
    }

    public List<OrderNotification> getByOrderId(String orderId) {
        return notificationRepository.findByOrderId(orderId);
    }
}
