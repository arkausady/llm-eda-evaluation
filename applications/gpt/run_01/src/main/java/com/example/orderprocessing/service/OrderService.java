package com.example.orderprocessing.service;

import com.example.orderprocessing.api.CreateOrderRequest;
import com.example.orderprocessing.event.OrderCreatedEvent;
import com.example.orderprocessing.event.OrderItemData;
import com.example.orderprocessing.exception.EventPublicationException;
import com.example.orderprocessing.exception.OrderNotFoundException;
import com.example.orderprocessing.model.CustomerOrder;
import com.example.orderprocessing.model.OrderItem;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CustomerOrder create(CreateOrderRequest request) {
        var now = Instant.now();
        var items = request.items().stream()
                .map(item -> new OrderItem(item.productId(), item.quantity(), item.unitPrice()))
                .toList();
        var total = request.items().stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var order = new CustomerOrder(UUID.randomUUID(), request.customerId(), request.customerEmail(),
                total, OrderStatus.PENDING, now, items);
        orderRepository.saveAndFlush(order);

        var eventItems = request.items().stream()
                .map(item -> new OrderItemData(item.productId(), item.quantity(), item.unitPrice()))
                .toList();
        var event = new OrderCreatedEvent(UUID.randomUUID(), order.getId(), request.customerId(),
                request.customerEmail(), request.paymentToken(), total, eventItems, now);
        try {
            eventPublisher.publish("order-events", order.getId().toString(), event);
        } catch (EventPublicationException exception) {
            order.changeStatus(OrderStatus.EVENT_PUBLICATION_FAILED, exception.getMessage());
            orderRepository.save(order);
            return order;
        }
        return order;
    }

    @Transactional(readOnly = true)
    public CustomerOrder get(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
