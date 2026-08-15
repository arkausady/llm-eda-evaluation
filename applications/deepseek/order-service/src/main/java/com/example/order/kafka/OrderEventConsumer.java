package com.example.order.kafka;

import com.example.common.event.*;
import com.example.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private final OrderService orderService;

    public OrderEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        if (event.isSuccess()) {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    public void consumePaymentFailed(PaymentFailedEvent event) {
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAYMENT_FAILED);
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-service")
    public void consumeInventoryUpdated(InventoryUpdatedEvent event) {
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.COMPLETED);
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-service")
    public void consumeInventoryFailed(InventoryFailedEvent event) {
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.FAILED);
    }
}
