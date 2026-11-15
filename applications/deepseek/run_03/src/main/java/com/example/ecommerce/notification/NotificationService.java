package com.example.ecommerce.notification;

import com.example.ecommerce.model.InventoryUpdatedEvent;
import com.example.ecommerce.model.OrderStatusNotification;
import com.example.ecommerce.model.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final Map<String, OrderStatusNotification> notifications = new ConcurrentHashMap<>();

    public void sendPaymentFailureNotification(PaymentProcessedEvent event) {
        OrderStatusNotification notification = new OrderStatusNotification(
                event.getOrderId(),
                "PAYMENT_FAILED",
                event.getMessage()
        );
        notifications.put(event.getOrderId(), notification);
        log.info("Payment failure notification for order {}: {}", event.getOrderId(), event.getMessage());
    }

    public void sendInventoryNotification(InventoryUpdatedEvent event) {
        String status = event.isSuccess() ? "INVENTORY_UPDATED" : "INVENTORY_UPDATE_FAILED";
        OrderStatusNotification notification = new OrderStatusNotification(
                event.getOrderId(),
                status,
                event.getMessage()
        );
        notifications.put(event.getOrderId(), notification);
        log.info("Inventory notification for order {}: {} - {}", event.getOrderId(), status, event.getMessage());
    }

    public OrderStatusNotification getNotification(String orderId) {
        return notifications.get(orderId);
    }
}
