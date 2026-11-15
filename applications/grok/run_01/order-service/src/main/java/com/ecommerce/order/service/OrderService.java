package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.shared.event.InventoryEvent;
import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.event.PaymentEvent;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.ecommerce.shared.model.OrderItem;
import com.ecommerce.shared.model.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderRepository orderRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Order createOrder(CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        BigDecimal total = items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(
                UUID.randomUUID().toString(),
                request.getCustomerId(),
                items,
                total,
                OrderStatus.CREATED,
                Instant.now()
        );
        orderRepository.save(order);

        publishOrderCreated(order);
        order.updateStatus(OrderStatus.PAYMENT_PENDING, null);
        orderRepository.save(order);

        log.info("Created order {} for customer {} total={}", order.getOrderId(), order.getCustomerId(), total);
        return order;
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void applyPaymentEvent(PaymentEvent event) {
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            switch (event.eventType()) {
                case "PAYMENT_COMPLETED" -> order.updateStatus(OrderStatus.PAID, null);
                case "PAYMENT_FAILED" -> order.updateStatus(OrderStatus.PAYMENT_FAILED, event.reason());
                case "PAYMENT_REFUNDED" -> order.updateStatus(OrderStatus.CANCELLED, event.reason());
                default -> log.warn("Unknown payment event type {}", event.eventType());
            }
            orderRepository.save(order);
            log.info("Order {} status updated to {} from payment event {}",
                    order.getOrderId(), order.getStatus(), event.eventType());
        }, () -> log.warn("Payment event for unknown order {}", event.orderId()));
    }

    public void applyInventoryEvent(InventoryEvent event) {
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            switch (event.eventType()) {
                case "INVENTORY_RESERVED" -> order.updateStatus(OrderStatus.CONFIRMED, null);
                case "INVENTORY_FAILED" -> order.updateStatus(OrderStatus.CANCELLED, event.reason());
                default -> log.warn("Unknown inventory event type {}", event.eventType());
            }
            orderRepository.save(order);
            log.info("Order {} status updated to {} from inventory event {}",
                    order.getOrderId(), order.getStatus(), event.eventType());
        }, () -> log.warn("Inventory event for unknown order {}", event.orderId()));
    }

    private void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.create(
                order.getOrderId(),
                order.getCustomerId(),
                order.getItems(),
                order.getTotalAmount()
        );
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.ORDER_EVENTS, order.getOrderId(), payload);
            log.info("Published ORDER_CREATED for order {} to {}", order.getOrderId(), KafkaTopics.ORDER_EVENTS);
        } catch (JsonProcessingException e) {
            order.updateStatus(OrderStatus.FAILED, "Failed to serialize order-created event");
            orderRepository.save(order);
            throw new IllegalStateException("Unable to publish order-created event", e);
        }
    }
}
