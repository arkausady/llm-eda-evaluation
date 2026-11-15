package com.ecommerce.notification.service;

import com.ecommerce.notification.domain.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.shared.event.EventTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(new NotificationRepository());
    }

    @Test
    void createsNotificationForOrderCreated() {
        ObjectNode payload = new ObjectMapper().createObjectNode();
        payload.put("eventType", EventTypes.ORDER_CREATED);
        payload.put("orderId", "order-1");

        Notification notification = notificationService.handleEvent("order-events", payload);

        assertEquals("order-1", notification.getOrderId());
        assertEquals(EventTypes.ORDER_CREATED, notification.getEventType());
        assertTrue(notification.getMessage().contains("awaiting payment"));
    }

    @Test
    void createsNotificationForPaymentFailure() {
        ObjectNode payload = new ObjectMapper().createObjectNode();
        payload.put("eventType", EventTypes.PAYMENT_FAILED);
        payload.put("orderId", "order-2");
        payload.put("reason", "declined");

        Notification notification = notificationService.handleEvent("payment-events", payload);

        assertTrue(notification.getMessage().contains("declined"));
        assertEquals(1, notificationService.findByOrderId("order-2").size());
    }
}
