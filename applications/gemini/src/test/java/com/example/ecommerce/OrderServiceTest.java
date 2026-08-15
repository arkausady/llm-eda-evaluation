package com.example.ecommerce.service;

import com.example.ecommerce.dto.CreateOrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.model.OrderEntity;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OrderService orderService;

    @BeforeEach;
    void setUp() {
        orderService = new OrderService(orderRepository, kafkaTemplate);
        ReflectionTestUtils.setField(orderService, "orderEventsTopic", "order-events");
    }

    @Test
    void testCreateOrder() {
        CreateOrderRequest request = new CreateOrderRequest("cust-1", "prod-1", 2, 25.0);
        OrderEntity mockSavedEntity = new OrderEntity("order-123", "cust-1", "prod-1", 2, 50.0, OrderStatus.PENDING);

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(mockSavedEntity);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("order-123", response.orderId());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(50.0, response.totalAmount());

        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        verify(kafkaTemplate, times(1)).send(eq("order-events"), eq("order-123"), any());
    }

    @Test
    void testGetOrder() {
        OrderEntity mockEntity = new OrderEntity("order-123", "cust-1", "prod-1", 2, 50.0, OrderStatus.COMPLETED);
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(mockEntity));

        OrderResponse response = orderService.getOrder("order-123");

        assertNotNull(response);
        assertEquals("order-123", response.orderId());
        assertEquals(OrderStatus.COMPLETED, response.status());
    }
}