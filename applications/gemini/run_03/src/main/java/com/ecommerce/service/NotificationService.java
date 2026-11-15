package com.ecommerce.service;

import com.ecommerce.config.KafkaConfig;
import com.ecommerce.events.InventoryEvent;
import com.ecommerce.events.OrderEvent;
import com.ecommerce.events.PaymentEvent;
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
     * FR5: Generate order status notification on Order Creation.
     */
    @KafkaListener(topics = KafkaConfig.ORDER_EVENTS_TOPIC, groupId = "notification-service-group")
    @Transactional
    public void onOrderEvent(OrderEvent event) {
        String msg = String.format("Order [%s] has been created for product [%s] (Qty: %d, Total: $%s).",
                event.orderId(), event.productId(), event.quantity(), event.totalAmount());
        saveAndSendNotification(event.orderId(), event.customerId(), msg, "ORDER_CREATED");
    }

    /**
     * FR5 & FR6: Generate order status notification on Payment Success, Failure, or Refund.
     */
    @KafkaListener(topics = KafkaConfig.PAYMENT_EVENTS_TOPIC, groupId = "notification-service-group")
    @Transactional
    public void onPaymentEvent(PaymentEvent event) {
        String msg;
        String statusType;
        if (event.status() == PaymentEvent.PaymentStatus.SUCCESS) {
            msg = String.format("Payment of $%s for Order [%s] was processed successfully.", event.amount(), event.orderId());
            statusType = "PAYMENT_SUCCESS";
        } else if (event.status() == PaymentEvent.PaymentStatus.REFUNDED) {
            msg = String.format("Payment for Order [%s] has been refunded. Reason: %s", event.orderId(), event.reason());
            statusType = "PAYMENT_REFUNDED";
        } else {
            msg = String.format("Payment for Order [%s] failed. Reason: %s", event.orderId(), event.reason());
            statusType = "PAYMENT_FAILED";
        }
        saveAndSendNotification(event.orderId(), event.customerId(), msg, statusType);
    }

    /**
     * FR5 & FR6: Generate order status notification on Inventory Reservation or Out-Of-Stock Failure.
     */
    @KafkaListener(topics = KafkaConfig.INVENTORY_EVENTS_TOPIC, groupId = "notification-service-group")
    @Transactional
    public void onInventoryEvent(InventoryEvent event) {
        String msg;
        String statusType;
        if (event.status() == InventoryEvent.InventoryStatus.RESERVED) {
            msg = String.format("Order [%s] is COMPLETED! Inventory (%d units of %s) reserved and ready for dispatch.",
                    event.orderId(), event.quantity(), event.productId());
            statusType = "ORDER_COMPLETED";
        } else {
            msg = String.format("Order [%s] FAILED due to inventory issue: %s", event.orderId(), event.reason());
            statusType = "INVENTORY_FAILED";
        }
        saveAndSendNotification(event.orderId(), event.customerId(), msg, statusType);
    }

    private void saveAndSendNotification(UUID orderId, String customerId, String message, String statusType) {
        log.info("NOTIFICATION -> Customer [{}]: {} ({})", customerId, message, statusType);
        NotificationLog logEntry = new NotificationLog(orderId, customerId, message, statusType);
        notificationRepository.save(logEntry);
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> getNotificationsForOrder(UUID orderId) {
        return notificationRepository.findByOrderIdOrderBySentAtAsc(orderId);
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
