package com.ecommerce.service;

import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.model.NotificationLog;
import com.ecommerce.repository.NotificationRepository;
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

    /**
     * FR5: Generate order status notification when order is created.
     */
    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.order-events}", groupId = "notification-service-group")
    public void onOrderEvent(OrderEvent event) {
        String message = String.format("Order %s created for customer %s. Status: %s. %s",
                event.orderId(), event.customerId(), event.status(), event.message());
        saveAndSendNotification(event.orderId(), event.customerId(), "ORDER_" + event.status().name(), message);
    }

    /**
     * FR5 & FR6: Generate order status notification for payment success, failure, or refund.
     */
    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "notification-service-group")
    public void onPaymentEvent(PaymentEvent event) {
        String message = String.format("Payment update for Order %s (Customer %s): Status=%s, Reason=%s",
                event.orderId(), event.customerId(), event.status(), event.reason());
        saveAndSendNotification(event.orderId(), event.customerId(), "PAYMENT_" + event.status().name(), message);
    }

    /**
     * FR5 & FR6: Generate order status notification for inventory reservation or out-of-stock failure.
     */
    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.inventory-events}", groupId = "notification-service-group")
    public void onInventoryEvent(InventoryEvent event) {
        String finalStatus = switch (event.status()) {
            case RESERVED -> "ORDER_COMPLETED";
            case OUT_OF_STOCK -> "ORDER_FAILED_OUT_OF_STOCK";
        };
        String message = String.format("Inventory update for Order %s (Customer %s): Status=%s, Details=%s",
                event.orderId(), event.customerId(), finalStatus, event.reason());
        saveAndSendNotification(event.orderId(), event.customerId(), finalStatus, message);
    }

    private void saveAndSendNotification(UUID orderId, String customerId, String status, String message) {
        NotificationLog notification = new NotificationLog(orderId, customerId, status, message);
        notificationRepository.save(notification);
        log.info("[NOTIFICATION SENT] To Customer {}: {}", customerId, message);
    }

    public List<NotificationLog> getNotificationsForOrder(UUID orderId) {
        return notificationRepository.findByOrderIdOrderByTimestampAsc(orderId);
    }

    public List<NotificationLog> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
