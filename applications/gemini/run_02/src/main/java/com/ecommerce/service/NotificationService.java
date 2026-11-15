package com.ecommerce.service;

import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.InventoryUpdatedEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.PaymentProcessedEvent;
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

    // FR5: Generate order status notification (Order Created)
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "notification-service-group")
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        String message = String.format("Order %s created for product %s (qty: %d). Total: %s",
                event.orderId(), event.productId(), event.quantity(), event.totalAmount());
        saveAndSendNotification(event.orderId(), event.customerId(), "ORDER_CREATED", message);
    }

    // FR5 & FR6: Generate order status notification (Payment Status)
    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "notification-service-group")
    @Transactional
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        String status = event.success() ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED";
        String message = event.success()
                ? String.format("Payment of %s succeeded for order %s.", event.totalAmount(), event.orderId())
                : String.format("Payment failed for order %s. Reason: %s", event.orderId(), event.reason());
        saveAndSendNotification(event.orderId(), event.customerId(), status, message);
    }

    // FR5 & FR6: Generate order status notification (Inventory & Final Order Status)
    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "notification-service-group")
    @Transactional
    public void onInventoryUpdated(InventoryUpdatedEvent event) {
        String status = event.success() ? "ORDER_COMPLETED" : "ORDER_FAILED_INVENTORY";
        String message = event.success()
                ? String.format("Order %s is confirmed and ready for shipment!", event.orderId())
                : String.format("Order %s cancelled due to inventory issue: %s. Payment will be refunded.", event.orderId(), event.reason());
        saveAndSendNotification(event.orderId(), event.customerId(), status, message);
    }

    private void saveAndSendNotification(UUID orderId, String customerId, String status, String message) {
        NotificationLog notification = new NotificationLog(orderId, customerId, status, message);
        notificationRepository.save(notification);
        log.info("NOTIFICATION TO CUSTOMER [{}]: [{}] - {}", customerId, status, message);
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> getNotificationsForOrder(UUID orderId) {
        return notificationRepository.findByOrderIdOrderByTimestampAsc(orderId);
    }
}