package com.example.ecommerce.notification;

import com.example.ecommerce.model.InventoryUpdatedEvent;
import com.example.ecommerce.model.OrderStatusNotification;
import com.example.ecommerce.model.PaymentProcessedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    @Test
    void sendPaymentFailureNotificationShouldStoreNotification() {
        NotificationService service = new NotificationService();
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                "order1", "FAILED", "Payment failed", new BigDecimal("1500.00")
        );
        service.sendPaymentFailureNotification(event);

        OrderStatusNotification notification = service.getNotification("order1");
        assertNotNull(notification);
        assertEquals("PAYMENT_FAILED", notification.getStatus());
        assertEquals("Payment failed", notification.getMessage());
    }

    @Test
    void sendInventoryNotificationShouldStoreNotification() {
        NotificationService service = new NotificationService();
        InventoryUpdatedEvent event = new InventoryUpdatedEvent(
                "order1", "prod1", 1, true, "Inventory updated"
        );
        service.sendInventoryNotification(event);

        OrderStatusNotification notification = service.getNotification("order1");
        assertNotNull(notification);
        assertEquals("INVENTORY_UPDATED", notification.getStatus());
        assertEquals("Inventory updated", notification.getMessage());
    }
}
