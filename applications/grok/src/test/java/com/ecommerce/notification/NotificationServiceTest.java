package com.ecommerce.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.InventoryEventType;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.OrderEventType;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentEventType;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.NotificationMessage;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.PaymentRecord;
import com.ecommerce.model.PaymentStatus;

class NotificationServiceTest {

    private NotificationRepository repository;
    private NotificationService notificationService;
    private CustomerOrder order;

    @BeforeEach
    void setUp() {
        repository = new NotificationRepository();
        notificationService = new NotificationService(repository);
        order = CustomerOrder.create(
                "cust-1",
                "buyer@example.com",
                List.of(new OrderItem("SKU-001", "Headphones", 1, new BigDecimal("79.99"))),
                "CARD"
        );
    }

    @Test
    void createsOrderCreatedNotification() {
        NotificationMessage message = notificationService.notifyOrderEvent(
                OrderEvent.of(OrderEventType.ORDER_CREATED, order, null));
        assertEquals("Order received", message.getSubject());
        assertEquals("buyer@example.com", message.getCustomerEmail());
        assertEquals(1, notificationService.getByOrderId(order.getOrderId()).size());
    }

    @Test
    void createsPaymentFailedNotification() {
        PaymentRecord payment = PaymentRecord.of(order.getOrderId(), order.getTotalAmount(), "CARD", PaymentStatus.FAILED, "declined");
        NotificationMessage message = notificationService.notifyPaymentEvent(
                PaymentEvent.from(PaymentEventType.PAYMENT_FAILED, payment, order, "declined"));
        assertEquals("Payment failed", message.getSubject());
        assertTrue(message.getBody().contains("declined"));
    }

    @Test
    void createsInventoryReservedNotification() {
        NotificationMessage message = notificationService.notifyInventoryEvent(
                InventoryEvent.of(InventoryEventType.INVENTORY_RESERVED, order, null));
        assertEquals("Items reserved", message.getSubject());
    }
}
