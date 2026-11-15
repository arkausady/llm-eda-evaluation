package com.example.ecommerce.service;

import com.example.ecommerce.config.KafkaTopics;
import com.example.ecommerce.event.InventoryEvent;
import com.example.ecommerce.event.InventoryStatus;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.event.PaymentStatus;
import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class OrderStatusService {
    private final OrderRepository orderRepository;

    public OrderStatusService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "order-status-payment-service")
    public void handlePaymentStatus(PaymentEvent event) {
        CustomerOrder order = findOrder(event.orderId());
        if (event.status() == PaymentStatus.COMPLETED) {
            order.updateStatus(OrderStatus.PAYMENT_COMPLETED, null);
        } else {
            order.updateStatus(OrderStatus.FAILED, event.reason());
        }
        orderRepository.save(order);
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "order-status-inventory-service")
    public void handleInventoryStatus(InventoryEvent event) {
        CustomerOrder order = findOrder(event.orderId());
        if (event.status() == InventoryStatus.UPDATED) {
            order.updateStatus(OrderStatus.COMPLETED, null);
        } else {
            order.updateStatus(OrderStatus.FAILED, event.reason());
        }
        orderRepository.save(order);
    }

    private CustomerOrder findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }
}
