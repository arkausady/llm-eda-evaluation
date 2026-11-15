package com.ecommerce.service;

import com.ecommerce.config.KafkaTopics;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.event.InventoryUpdatedEvent;
import com.ecommerce.event.OrderCreatedEvent;
import com.ecommerce.event.PaymentProcessedEvent;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // FR1a: Create and store a customer order & FR2: Publish an order-created event
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(
                orderId,
                request.customerId(),
                request.productId(),
                request.quantity(),
                request.totalAmount(),
                OrderStatus.CREATED
        );

        orderRepository.save(order);
        log.info("Order stored in database: {}", orderId);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalAmount(),
                Instant.now()
        );

        kafkaTemplate.send(KafkaTopics.ORDER_EVENTS, order.getId().toString(), event);
        log.info("Published OrderCreatedEvent to topic {} for order {}", KafkaTopics.ORDER_EVENTS, orderId);

        return OrderResponse.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrder(UUID orderId) {
        return orderRepository.findById(orderId).map(OrderResponse::fromEntity);
    }

    // FR6: Handle payment status updates and failures
    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "order-service-group")
    @Transactional
    public void handlePaymentEvent(PaymentProcessedEvent event) {
        log.info("OrderService received PaymentProcessedEvent for order {}: success={}", event.orderId(), event.success());
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (event.success()) {
                if (order.getStatus() == OrderStatus.CREATED) {
                    order.setStatus(OrderStatus.PAYMENT_COMPLETED);
                }
            } else {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                order.setFailureReason(event.reason());
            }
            orderRepository.save(order);
        });
    }

    // FR6: Handle inventory updates and failures (compensation)
    @KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "order-service-group")
    @Transactional
    public void handleInventoryEvent(InventoryUpdatedEvent event) {
        log.info("OrderService received InventoryUpdatedEvent for order {}: success={}", event.orderId(), event.success());
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (event.success()) {
                order.setStatus(OrderStatus.COMPLETED);
            } else {
                order.setStatus(OrderStatus.INVENTORY_FAILED);
                order.setFailureReason(event.reason());
            }
            orderRepository.save(order);
        });
    }
}