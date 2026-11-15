package com.ecommerce.notification.service;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.event.PaymentStatus;
import com.ecommerce.common.model.OrderItem;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(new NotificationRepository());
    }

    @Test
    void createsNotificationsForLifecycleEvents() {
        OrderCreatedEvent created = new OrderCreatedEvent(
                "e1", "order-1", "cust-1",
                List.of(new OrderItem("SKU-001", 1, new BigDecimal("10.00"))),
                new BigDecimal("10.00"),
                Instant.now()
        );
        PaymentEvent paid = PaymentEvent.success(created);
        InventoryEvent reserved = InventoryEvent.reserved(paid);

        notificationService.notifyOrderCreated(created);
        notificationService.notifyPayment(paid);
        notificationService.notifyInventory(reserved);

        List<Notification> notifications = notificationService.findByOrderId("order-1");
        assertThat(notifications).hasSize(3);
        assertThat(notifications).extracting(Notification::type)
                .containsExactly("ORDER_CREATED", "PAYMENT_SUCCESS", "INVENTORY_RESERVED");
    }

    @Test
    void createsFailureNotificationForPayment() {
        OrderCreatedEvent created = new OrderCreatedEvent(
                "e1", "order-2", "cust-FAIL",
                List.of(new OrderItem("SKU-001", 1, new BigDecimal("10.00"))),
                new BigDecimal("10.00"),
                Instant.now()
        );
        PaymentEvent failed = PaymentEvent.failed(created, "Payment declined for customer");

        Notification notification = notificationService.notifyPayment(failed);

        assertThat(notification.type()).isEqualTo("PAYMENT_FAILED");
        assertThat(notification.message()).contains("Payment declined");
        assertThat(notificationService.findAll()).hasSize(1);
    }
}
