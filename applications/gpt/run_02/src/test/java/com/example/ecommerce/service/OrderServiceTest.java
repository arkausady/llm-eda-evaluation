package com.example.ecommerce.service;

import com.example.ecommerce.api.CreateOrderRequest;
import com.example.ecommerce.config.KafkaTopics;
import com.example.ecommerce.event.OrderCreatedEvent;
import com.example.ecommerce.messaging.EventPublisher;
import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EventPublisher eventPublisher;

    @Test
    void createsStoresAndPublishesOrder() {
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventPublisher.publish(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        OrderService service = new OrderService(orderRepository, eventPublisher);

        CustomerOrder order = service.createOrder(new CreateOrderRequest(
                "customer-1", "product-1", 2, new BigDecimal("29.99"), "CARD-123"));

        assertNotNull(order.getId());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        verify(orderRepository).save(order);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publish(org.mockito.ArgumentMatchers.eq(KafkaTopics.ORDER_EVENTS),
                org.mockito.ArgumentMatchers.eq(order.getId()), eventCaptor.capture());
        OrderCreatedEvent event = (OrderCreatedEvent) eventCaptor.getValue();
        assertEquals(order.getId(), event.orderId());
        assertEquals("product-1", event.productId());
    }
}
