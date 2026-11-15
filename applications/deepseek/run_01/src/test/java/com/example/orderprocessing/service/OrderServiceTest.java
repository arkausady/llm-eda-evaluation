package com.example.orderprocessing.service;

import com.example.orderprocessing.dto.OrderRequest;
import com.example.orderprocessing.dto.OrderResponse;
import com.example.orderprocessing.event.KafkaTopics;
import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest request;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        request = new OrderRequest("cust1", "prod1", 2, new BigDecimal("100.00"));
        savedOrder = new Order("cust1", "prod1", 2, new BigDecimal("100.00"), OrderStatus.CREATED, LocalDateTime.now());
        savedOrder.setId(1L);
    }

    @Test
    void createOrderShouldSaveAndPublishEvent() {
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals(OrderStatus.CREATED.name(), response.status());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(eq(KafkaTopics.ORDER_EVENTS), eq("1"), any());
    }
}