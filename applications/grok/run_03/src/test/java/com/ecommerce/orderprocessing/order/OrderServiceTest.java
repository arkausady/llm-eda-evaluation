package com.ecommerce.orderprocessing.order;

import com.ecommerce.orderprocessing.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.dto.OrderItemRequest;
import com.ecommerce.orderprocessing.event.OrderCreatedEvent;
import com.ecommerce.orderprocessing.exception.InvalidOrderException;
import com.ecommerce.orderprocessing.exception.OrderNotFoundException;
import com.ecommerce.orderprocessing.exception.OrderProcessingException;
import com.ecommerce.orderprocessing.model.CustomerOrder;
import com.ecommerce.orderprocessing.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderEventProducer orderEventProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(new OrderRepository(), orderEventProducer);
    }

    @Test
    void createOrderStoresAndPublishesEvent() {
        CreateOrderRequest request = sampleRequest("CREDIT_CARD", 1);
        CustomerOrder order = orderService.createOrder(request);

        assertNotNull(order.orderId());
        assertEquals(OrderStatus.PAYMENT_PENDING, order.status());
        assertEquals(new BigDecimal("999.99"), order.totalAmount());
        verify(orderEventProducer).publishOrderCreated(any(OrderCreatedEvent.class));
        assertEquals(order.orderId(), orderService.getOrder(order.orderId()).orderId());
    }

    @Test
    void createOrderRejectsZeroTotal() {
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-1",
                "alice@example.com",
                "CREDIT_CARD",
                List.of(new OrderItemRequest("PROD-001", "Laptop", 1, new BigDecimal("0.00")))
        );
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request));
        verify(orderEventProducer, never()).publishOrderCreated(any());
    }

    @Test
    void publishFailureMarksOrderFailed() {
        doThrow(new OrderProcessingException("kafka down"))
                .when(orderEventProducer).publishOrderCreated(any());
        assertThrows(OrderProcessingException.class, () -> orderService.createOrder(sampleRequest("CREDIT_CARD", 1)));
        assertEquals(1, orderService.getOrders().size());
        assertEquals(OrderStatus.FAILED, orderService.getOrders().getFirst().status());
    }

    @Test
    void getUnknownOrderThrows() {
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder("missing"));
    }

    @Test
    void updateStatusChangesStoredOrder() {
        CustomerOrder created = orderService.createOrder(sampleRequest("CREDIT_CARD", 1));
        orderService.updateStatus(created.orderId(), OrderStatus.PAID, "ok");
        assertEquals(OrderStatus.PAID, orderService.getOrder(created.orderId()).status());
    }

    @Test
    void publishedEventContainsOrderId() {
        CustomerOrder created = orderService.createOrder(sampleRequest("CREDIT_CARD", 1));
        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderEventProducer).publishOrderCreated(captor.capture());
        assertEquals(created.orderId(), captor.getValue().orderId());
        assertEquals(OrderCreatedEvent.TYPE, captor.getValue().eventType());
    }

    private CreateOrderRequest sampleRequest(String method, int quantity) {
        return new CreateOrderRequest(
                "CUST-1",
                "alice@example.com",
                method,
                List.of(new OrderItemRequest("PROD-001", "Laptop", quantity, new BigDecimal("999.99")))
        );
    }
}
