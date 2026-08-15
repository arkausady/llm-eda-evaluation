package com.example.orderprocessing.order;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderWorkflowListener {
    private final OrderService orderService;

    public OrderWorkflowListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-payment-status")
    public void paymentEvent(BusinessEvent event) {
        if (event.type() == EventType.PAYMENT_SUCCEEDED || event.type() == EventType.PAYMENT_FAILED) {
            orderService.handlePaymentEvent(event);
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-inventory-status")
    public void inventoryEvent(BusinessEvent event) {
        if (event.type() == EventType.INVENTORY_RESERVED || event.type() == EventType.INVENTORY_FAILED) {
            orderService.handleInventoryEvent(event);
        }
    }
}
