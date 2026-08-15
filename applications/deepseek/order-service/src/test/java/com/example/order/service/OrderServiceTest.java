package com.example.order.service;

import com.example.common.event.OrderCreatedEvent;
import com.example.order.kafka.OrderEventPublisher;
import com.example.order.model.Order;
import com.example.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderEventPublisher eventPublisher;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(OrderEventPublisher.class);
        orderService = new OrderService(orderRepository, eventPublisher);
    }

    @Test
    void createOrder_publishesEvent() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int quantity = 2;
        double total = 99.0;

        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setCustomerId(customerId);
        savedOrder.setProductId(productId);
        savedOrder.setQuantity(quantity);
        savedOrder.setTotalAmount(total);
        savedOrder.setStatus(com.example.common.event.OrderStatus.CREATED);
        savedOrder.setCreatedAt(java.time.Instant.now());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order created = orderService.createOrder(customerId, productId, quantity, total);

        assertNotNull(created);
        assertEquals(customerId, created.getCustomerId());
        verify(eventPublisher).publishOrderCreated(any(OrderCreatedEvent.class));

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher).publishOrderCreated(captor.capture());
        assertEquals(savedOrder.getId(), captor.getValue().getOrderId());
    }
}
