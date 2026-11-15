package com.example.ecommerce.order;

import com.example.ecommerce.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderShouldSaveAndPublishEvent() {
        OrderController.OrderRequest request = new OrderController.OrderRequest();
        request.setCustomerId("cust1");
        request.setProductId("prod1");
        request.setQuantity(2);
        request.setAmount(new BigDecimal("200.00"));

        Order order = orderService.createOrder(request);

        assertNotNull(order);
        assertEquals("cust1", order.getCustomerId());
        assertEquals("prod1", order.getProductId());
        assertEquals(2, order.getQuantity());
        assertEquals(new BigDecimal("200.00"), order.getAmount());
        assertEquals(Order.OrderStatus.CREATED, order.getStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventPublisher, times(1)).publishOrderCreated(any(Order.class));
    }
}
