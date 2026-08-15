package com.ecommerce.notification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.NotificationMessage;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationMessage notifyOrderEvent(OrderEvent event) {
        CustomerOrder order = event.getOrder();
        String subject = switch (event.getEventType()) {
            case ORDER_CREATED -> "Order received";
            case ORDER_COMPLETED -> "Order completed";
            case ORDER_CANCELLED -> "Order cancelled";
            case ORDER_FAILED -> "Order failed";
        };
        String body = switch (event.getEventType()) {
            case ORDER_CREATED -> "We received order " + event.getOrderId() + " and started processing.";
            case ORDER_COMPLETED -> "Order " + event.getOrderId() + " was completed successfully.";
            case ORDER_CANCELLED -> "Order " + event.getOrderId() + " was cancelled. " + safe(event.getReason());
            case ORDER_FAILED -> "Order " + event.getOrderId() + " could not be processed. " + safe(event.getReason());
        };
        return store(order, subject, body);
    }

    public NotificationMessage notifyPaymentEvent(PaymentEvent event) {
        CustomerOrder order = event.getOrder();
        String subject = switch (event.getEventType()) {
            case PAYMENT_COMPLETED -> "Payment confirmed";
            case PAYMENT_FAILED -> "Payment failed";
            case PAYMENT_REFUNDED -> "Payment refunded";
        };
        String body = switch (event.getEventType()) {
            case PAYMENT_COMPLETED -> "Payment " + event.getPaymentId() + " for order " + event.getOrderId() + " was successful.";
            case PAYMENT_FAILED -> "Payment for order " + event.getOrderId() + " failed. " + safe(event.getReason());
            case PAYMENT_REFUNDED -> "Payment for order " + event.getOrderId() + " was refunded. " + safe(event.getReason());
        };
        return store(order, subject, body);
    }

    public NotificationMessage notifyInventoryEvent(InventoryEvent event) {
        CustomerOrder order = event.getOrder();
        String subject = switch (event.getEventType()) {
            case INVENTORY_RESERVED -> "Items reserved";
            case INVENTORY_FAILED -> "Inventory issue";
        };
        String body = switch (event.getEventType()) {
            case INVENTORY_RESERVED -> "Inventory was reserved for order " + event.getOrderId() + ".";
            case INVENTORY_FAILED -> "We could not reserve inventory for order " + event.getOrderId() + ". " + safe(event.getReason());
        };
        return store(order, subject, body);
    }

    public List<NotificationMessage> getAll() {
        return notificationRepository.findAll();
    }

    public List<NotificationMessage> getByOrderId(String orderId) {
        return notificationRepository.findByOrderId(orderId);
    }

    private NotificationMessage store(CustomerOrder order, String subject, String body) {
        String email = order == null ? "unknown@example.com" : order.getCustomerEmail();
        String orderId = order == null ? "unknown" : order.getOrderId();
        NotificationMessage message = NotificationMessage.email(orderId, email, subject, body);
        notificationRepository.save(message);
        log.info("Notification [{}] for order {}: {}", message.getNotificationId(), orderId, subject);
        return message;
    }

    private String safe(String reason) {
        return reason == null ? "" : reason;
    }
}
