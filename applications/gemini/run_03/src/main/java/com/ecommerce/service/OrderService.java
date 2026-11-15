package com.ecommerce.service;

import com.ecommerce.config.KafkaConfig;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.events.InventoryEvent;
import com.ecommerce.events.OrderEvent;
import com.ecommerce.events.PaymentEvent;
import com.ecommerce.model.OrderEntity;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    /**
     * FR1a: Create and store a customer order.
     * FR2: Publish an order-created event.
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer: {}, product: {}, quantity: {}",
                request.customerId(), request.productId(), request.quantity());

        OrderEntity entity = new OrderEntity(
                request.customerId(),
                request.productId(),
                request.quantity(),
                request.totalAmount(),
                OrderStatus.CREATED
        );

        OrderEntity saved = orderRepository.save(entity);

        OrderEvent event = OrderEvent.created(
                saved.getId(),
                saved.getCustomerId(),
                saved.getProductId(),
                saved.getQuantity(),
                saved.getTotalAmount()
        );

        log.info("Publishing OrderCreatedEvent for orderId: {} to topic: {}", saved.getId(), KafkaConfig.ORDER_EVENTS_TOPIC);
        kafkaTemplate.send(KafkaConfig.ORDER_EVENTS_TOPIC, saved.getId().toString(), event);

        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    /**
     * Listen to payment events to update order status accordingly (FR6).
     */
    @KafkaListener(topics = KafkaConfig.PAYMENT_EVENTS_TOPIC, groupId = "order-service-group")
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("OrderService received PaymentEvent for orderId: {}, status: {}", event.orderId(), event.status());
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (event.status() == PaymentEvent.PaymentStatus.SUCCESS) {
                if (order.getStatus() == OrderStatus.CREATED) {
                    order.setStatus(OrderStatus.PAYMENT_PROCESSED);
                }
            } else if (event.status() == PaymentEvent.PaymentStatus.FAILED) {
                order.setStatus(OrderStatus.FAILED);
                order.setFailureReason(event.reason());
            } else if (event.status() == PaymentEvent.PaymentStatus.REFUNDED) {
                order.setStatus(OrderStatus.FAILED);
                order.setFailureReason(event.reason());
            }
            orderRepository.save(order);
        });
    }

    /**
     * Listen to inventory events to finalize order status (FR4 / FR6).
     */
    @KafkaListener(topics = KafkaConfig.INVENTORY_EVENTS_TOPIC, groupId = "order-service-group")
    @Transactional
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("OrderService received InventoryEvent for orderId: {}, status: {}", event.orderId(), event.status());
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (event.status() == InventoryEvent.InventoryStatus.RESERVED) {
                order.setStatus(OrderStatus.COMPLETED);
            } else if (event.status() == InventoryEvent.InventoryStatus.OUT_OF_STOCK) {
                order.setStatus(OrderStatus.FAILED);
                order.setFailureReason(event.reason());
            }
            orderRepository.save(order);
        });
    }
}
