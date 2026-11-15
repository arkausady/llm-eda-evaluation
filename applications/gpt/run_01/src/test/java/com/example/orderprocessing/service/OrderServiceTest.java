package com.example.orderprocessing.service;

import com.example.orderprocessing.api.CreateOrderRequest;
import com.example.orderprocessing.exception.EventPublicationException;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderServiceTest {
    private OrderRepository orderRepository;
    private EventPublisher eventPublisher;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(EventPublisher.class);
        orderService = new OrderService(orderRepository, eventPublisher);
    }

    @Test
    void createsOrderAndPublishesEvent() {
        var request = request("valid-token");

        var order = orderService.create(request);

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("25.00"), order.getTotalAmount());
        verify(orderRepository).saveAndFlush(order);
        verify(eventPublisher).publish("order-events", order.getId().toString(), any());
    }

    @Test
    void recordsEventPublicationFailure() {
        doThrow(new EventPublicationException("Kafka unavailable", new RuntimeException()))
                .when(eventPublisher).publish(any(), any(), any());

        var order = orderService.create(request("valid-token"));

        assertEquals(OrderStatus.EVENT_PUBLICATION_FAILED, order.getStatus());
        var captor = ArgumentCaptor.forClass(com.example.orderprocessing.model.CustomerOrder.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(OrderStatus.EVENT_PUBLICATION_FAILED, captor.getValue().getStatus());
    }

    private CreateOrderRequest request(String token) {
        return new CreateOrderRequest("customer-1", "customer@example.com", token,
                List.of(new CreateOrderRequest.Item("product-1", 2, new BigDecimal("12.50"))));
    }
}
