package com.example.ecommerce.service;

import com.example.ecommerce.dto.CreateOrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.event.OrderEvent;
import com.example.ecommerce.model.OrderEntity;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-events}")
    private String orderEventsTopic;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        double totalAmount = request.price() * request.quantity();

        OrderEntity entity = new OrderEntity(
                orderId,
                request.customerId(),
                request.productId(),
                request.quantity(),
                totalAmount,
                OrderStatus.PENDING
        );

        OrderEntity savedEntity = orderRepository.save(entity);
        log.info("FR1a: Saved order {} to database with status PENDING", orderId);

        OrderEvent orderEvent = new OrderEvent(
                savedEntity.getId(),
                savedEntity.getCustomerId(),
                savedEntity.getProductId(),
                savedEntity.getQuantity(),
                savedEntity.getTotalAmount(),
                "ORDER_CREATED",
                "Order created successfully",
                System.currentTimeMillis()
        );

        kafkaTemplate.send(orderEventsTopic, savedEntity.getId(), orderEvent);
        log.info("FR2: Published ORDER_CREATED event for order {}", orderId);

        return mapToResponse(savedEntity);
    }

    public OrderResponse getOrder(String orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return mapToResponse(entity);
    }

    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus status, String failureReason) {
        orderRepository.findById(orderId).ifPresent(entity -> {
            entity.setStatus(status);
            if (failureReason != null) {
                entity.setFailureReason(failureReason);
            }
            orderRepository.save(entity);
            log.info("Updated order {} status to {}", orderId, status);
        });
    }

    private OrderResponse mapToResponse(OrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getFailureReason()
        );
    }
}