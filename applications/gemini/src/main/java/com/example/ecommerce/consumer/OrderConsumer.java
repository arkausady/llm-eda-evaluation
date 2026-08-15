package com.example.ecommerce.consumer;

import com.example.ecommerce.event.InventoryEvent;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);
    private final OrderService orderService;

    public OrderConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "order-status-group")
    public void handlePaymentResult(PaymentEvent paymentEvent) {
        log.info("Order Consumer received PaymentEvent for order {}", paymentEvent.orderId());
        if ("SUCCESS".equals(paymentEvent.status())) {
            orderService.updateOrderStatus(paymentEvent.orderId(), OrderStatus.PAYMENT_SUCCESSFUL, null);
        } else {
            log.warn("FR6: Payment failed for order {}", paymentEvent.orderId());
            orderService.updateOrderStatus(paymentEvent.orderId(), OrderStatus.PAYMENT_FAILED, paymentEvent.reason());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.inventory-events}", groupId = "order-status-group")
    public void handleInventoryResult(InventoryEvent inventoryEvent) {
        log.info("Order Consumer received InventoryEvent for order {}", inventoryEvent.orderId());
        if ("SUCCESS".equals(inventoryEvent.status())) {
            orderService.updateOrderStatus(inventoryEvent.orderId(), OrderStatus.COMPLETED, null);
        } else {
            log.warn("FR6: Inventory allocation failed for order {}", inventoryEvent.orderId());
            orderService.updateOrderStatus(inventoryEvent.orderId(), OrderStatus.INVENTORY_FAILED, inventoryEvent.reason());
        }
    }
}