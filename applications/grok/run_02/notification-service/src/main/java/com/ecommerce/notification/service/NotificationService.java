package com.ecommerce.notification.service;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification notifyOrderCreated(OrderCreatedEvent event) {
        return save(event.orderId(), "ORDER_CREATED",
                "Order " + event.orderId() + " created for customer " + event.customerId());
    }

    public Notification notifyPayment(PaymentEvent event) {
        String message = event.status().name().equals("SUCCESS")
                ? "Payment succeeded for order " + event.orderId()
                : "Payment failed for order " + event.orderId() + ": " + event.reason();
        return save(event.orderId(), "PAYMENT_" + event.status(), message);
    }

    public Notification notifyInventory(InventoryEvent event) {
        String message = event.status().name().equals("RESERVED")
                ? "Inventory reserved and order " + event.orderId() + " completed"
                : "Inventory update failed for order " + event.orderId() + ": " + event.reason();
        return save(event.orderId(), "INVENTORY_" + event.status(), message);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public List<Notification> findByOrderId(String orderId) {
        return notificationRepository.findByOrderId(orderId);
    }

    private Notification save(String orderId, String type, String message) {
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                orderId,
                type,
                message,
                Instant.now()
        );
        notificationRepository.save(notification);
        log.info("Notification generated type={} orderId={}", type, orderId);
        return notification;
    }
}
