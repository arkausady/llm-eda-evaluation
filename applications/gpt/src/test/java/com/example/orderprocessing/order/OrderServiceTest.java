package com.example.orderprocessing.order;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventPublisher;
import com.example.orderprocessing.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository repository;
    @Mock
    private EventPublisher publisher;

    @Test
    void createsOrderAndPublishesEvent() {
        when(repository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OrderService service = new OrderService(repository, publisher);
        CreateOrderRequest request = new CreateOrderRequest("customer-1", new BigDecimal("49.99"),
                List.of(new OrderItem("SKU-100", 2)), "PAY-OK");

        OrderResponse response = service.create(request);

        assertNotNull(response.id());
        assertEquals(OrderStatus.CREATED, response.status());
        ArgumentCaptor<BusinessEvent> event = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(publisher).publish(org.mockito.ArgumentMatchers.eq("order-events"), event.capture());
        assertEquals(response.id(), event.getValue().orderId());
    }
}
