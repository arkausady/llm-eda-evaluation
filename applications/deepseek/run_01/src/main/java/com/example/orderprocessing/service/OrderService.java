package com.example.orderprocessing.service;

import com.example.orderprocessing.dto.OrderRequest;
import com.example.orderprocessing.dto.OrderResponse;
import com.example.orderprocessing.event.InventoryUpdatedEvent;
import com.example.orderprocessing.event.KafkaTopics;
import com.example.orderprocessing.event.OrderCreatedEvent;
import com.example.orderprocessing.event.PaymentProcessedEvent;
import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order(
                request.customerId(),
                request.productId(),
                request.quantity(),
                request.amount(),
                OrderStatus.CREATED,
                LocalDateTime.now()
        );
        order = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getAmount(),
                order.getCreatedAt()
        );
        kafkaTemplate.send(KafkaTopics.ORDER_EVENTS, String.valueOf(order.getId()), event);
        log.info("Published OrderCreatedEvent for order {}", order.getId());

        return new OrderResponse(order.getId(), order.getStatus().name(), "Order created successfully");
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "order-service-payment")
    public void handlePaymentEvent(PaymentProcessedEvent event) {
        try {
            Order order = orderRepository.findById(event.orderId()).orElse(null);
            if (order != null) {
                if ("SUCCESS".equals(event.status())) {
                    order.setStatus(OrderStatus.PAYMENT_SUCCESS);
                } else {
                    order.setStatus(OrderStatus.PAYMENT_FAILED);
                }
                orderRepository.save(order);
                log.info("Updated order {} status to {}", order.getId(), order.getStatus());
            } else {
                log.warn("Order {} not found for payment event", event.orderId());
            }
        } catch (Exception e) {
            log.error("Error handling payment event for order {}: {}", event.orderId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "order-service-inventory")
    public void handleInventoryEvent(InventoryUpdatedEvent event) {
        try {
            Order order = orderRepository.findById(event.orderId()).orElse(null);
            if (order != null) {
                if ("SUCCESS".equals(event.status())) {
                    order.setStatus(OrderStatus.INVENTORY_SUCCESS);
                } else {
                    order.setStatus(OrderStatus.INVENTORY_FAILED);
                }
                orderRepository.save(order);
                log.info("Updated order {} status to {}", order.getId(), order.getStatus());
            } else {
                log.warn("Order {} not found for inventory event", event.orderId());
            }
        } catch (Exception e) {
            log.error("Error handling inventory event for order {}: {}", event.orderId(), e.getMessage(), e);
        }
    }
}