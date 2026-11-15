package com.ecommerce.orderprocessing.notification;

import com.ecommerce.orderprocessing.model.OrderNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(new NotificationRepository());
    }

    @Test
    void storesNotificationPerOrder() {
        notificationService.notify("order-1", "a@example.com", "ORDER_CREATED", "created");
        notificationService.notify("order-1", "a@example.com", "PAYMENT_PROCESSED", "paid");
        notificationService.notify("order-2", "b@example.com", "ORDER_CREATED", "created");

        assertEquals(3, notificationService.getAll().size());
        assertEquals(2, notificationService.getByOrderId("order-1").size());
        OrderNotification first = notificationService.getByOrderId("order-1").getFirst();
        assertEquals("ORDER_CREATED", first.eventType());
    }
}
