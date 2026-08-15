package com.example.notification.service;

import com.example.common.event.*;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notifyOrderCreated(OrderCreatedEvent event) {
        System.out.println("NOTIFICATION: Order created with ID: " + event.getOrderId());
    }

    public void notifyPaymentProcessed(PaymentProcessedEvent event) {
        System.out.println("NOTIFICATION: Payment processed for order " + event.getOrderId() + ", success: " + event.isSuccess());
    }

    public void notifyPaymentFailed(PaymentFailedEvent event) {
        System.out.println("NOTIFICATION: Payment failed for order " + event.getOrderId() + ", reason: " + event.getReason());
    }

    public void notifyInventoryUpdated(InventoryUpdatedEvent event) {
        System.out.println("NOTIFICATION: Inventory updated for order " + event.getOrderId());
    }

    public void notifyInventoryFailed(InventoryFailedEvent event) {
        System.out.println("NOTIFICATION: Inventory failed for order " + event.getOrderId() + ", reason: " + event.getReason());
    }
}
