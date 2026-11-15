package com.example.ecommerce.service;

import com.example.ecommerce.api.CreateOrderRequest;
import com.example.ecommerce.config.KafkaTopics;
import com.example.ecommerce.event.OrderCreatedEvent;
import com.example.ecommerce.messaging.EventPublisher;
import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CustomerOrder createOrder(CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        CustomerOrder order = new CustomerOrder(orderId, request.customerId(), request.productId(),
                request.quantity(), request.totalAmount(), OrderStatus.CREATED, now);
        orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID().toString(), orderId,
                request.customerId(), request.productId(), request.quantity(), request.totalAmount(),
                request.paymentReference(), now);

        eventPublisher.publish(KafkaTopics.ORDER_EVENTS, orderId, event)
                .exceptionally(exception -> {
                    log.error("Unable to publish order-created event for order {}", orderId, exception);
                    markPublishFailure(orderId, rootMessage(exception));
                    return null;
                });
        return order;
    }

    public CustomerOrder getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }

    public List<CustomerOrder> getOrders() {
        return orderRepository.findAll();
    }

    void markPublishFailure(String orderId, String reason) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.updateStatus(OrderStatus.FAILED, "Order event publication failed: " + reason);
            orderRepository.save(order);
        });
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
