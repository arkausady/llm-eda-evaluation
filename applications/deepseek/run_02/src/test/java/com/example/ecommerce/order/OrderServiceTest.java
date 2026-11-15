package com.example.ecommerce.order;

import com.example.ecommerce.common.EventEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldPublishOrderCreatedEvent() {
        ReflectionTestUtils.setField(orderService, "orderEventsTopic", "order-events");

        OrderItem item = new OrderItem("P1", "Laptop", 1, BigDecimal.valueOf(1000));
        Order order = orderService.createOrder("C1", List.of(item));

        assertNotNull(order.getOrderId());
        assertEquals("CREATED", order.getStatus());
        assertEquals(BigDecimal.valueOf(1000), order.getTotalAmount());

        ArgumentCaptor<EventEnvelope> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(eq("order-events"), anyString(), captor.capture());

        EventEnvelope envelope = captor.getValue();
        assertEquals("OrderCreated", envelope.getEventType());
        assertTrue(envelope.getPayload() instanceof OrderCreatedEvent);
        OrderCreatedEvent event = (OrderCreatedEvent) envelope.getPayload();
        assertEquals(order.getOrderId(), event.getOrderId());
    }
}