package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.shared.event.InventoryEvent;
import com.ecommerce.shared.event.PaymentEvent;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.ecommerce.shared.model.OrderItem;
import com.ecommerce.shared.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        orderService = new OrderService(orderRepository, kafkaTemplate, objectMapper);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createOrderStoresOrderAndPublishesEvent() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("cust-1");
        CreateOrderRequest.Item item = new CreateOrderRequest.Item();
        item.setProductId("PROD-1");
        item.setProductName("Laptop");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("10.00"));
        request.setItems(List.of(item));

        Order order = orderService.createOrder(request);

        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());
        assertEquals(new BigDecimal("20.00"), order.getTotalAmount());
        verify(kafkaTemplate).send(eq(KafkaTopics.ORDER_EVENTS), eq(order.getOrderId()), any(String.class));
    }

    @Test
    void applyPaymentCompletedUpdatesStatus() {
        Order order = sampleOrder();
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

        PaymentEvent event = PaymentEvent.completed(
                order.getOrderId(), order.getCustomerId(), order.getItems(), order.getTotalAmount(), "tx-1");
        orderService.applyPaymentEvent(event);

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void applyInventoryReservedConfirmsOrder() {
        Order order = sampleOrder();
        order.updateStatus(OrderStatus.PAID, null);
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

        InventoryEvent event = InventoryEvent.reserved(order.getOrderId(), order.getCustomerId(), order.getItems());
        orderService.applyInventoryEvent(event);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void getUnknownOrderThrows() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder("missing"));
    }

    @Test
    void createOrderPublishesToOrderEventsTopic() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("cust-2");
        CreateOrderRequest.Item item = new CreateOrderRequest.Item();
        item.setProductId("PROD-2");
        item.setProductName("Mouse");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("5.00"));
        request.setItems(List.of(item));

        orderService.createOrder(request);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), any(String.class), any(String.class));
        assertEquals(KafkaTopics.ORDER_EVENTS, topicCaptor.getValue());
    }

    private Order sampleOrder() {
        return new Order(
                "order-1",
                "cust-1",
                List.of(new OrderItem("PROD-1", "Laptop", 1, new BigDecimal("999.99"))),
                new BigDecimal("999.99"),
                OrderStatus.PAYMENT_PENDING,
                java.time.Instant.parse("2024-01-01T00:00:00Z")
        );
    }
}
