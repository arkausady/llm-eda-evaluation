package com.example.ecommerce.service;

import com.example.ecommerce.entity.CustomerOrder;
import com.example.ecommerce.model.EventType;
import com.example.ecommerce.model.OrderEvent;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderStatusService {

    private final OrderRepository orderRepository;

    public OrderStatusService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-status-service")
    @Transactional
    public void handlePaymentStatus(OrderEvent event) {
        CustomerOrder order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.orderId()));

        if (order.isTerminal()) {
            return;
        }
        if (event.type() == EventType.PAYMENT_COMPLETED) {
            order.updateStatus(OrderStatus.PAYMENT_COMPLETED, null);
        } else if (event.type() == EventType.PAYMENT_FAILED) {
            order.updateStatus(OrderStatus.PAYMENT_FAILED, event.reason());
        } else {
            return;
        }
        orderRepository.save(order);
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-status-service")
    @Transactional
    public void handleInventoryStatus(OrderEvent event) {
        CustomerOrder order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.orderId()));

        if (event.type() == EventType.INVENTORY_RESERVED) {
            order.updateStatus(OrderStatus.COMPLETED, null);
        } else if (event.type() == EventType.INVENTORY_FAILED) {
            order.updateStatus(OrderStatus.INVENTORY_FAILED, event.reason());
        } else {
            return;
        }
        orderRepository.save(order);
    }
}
