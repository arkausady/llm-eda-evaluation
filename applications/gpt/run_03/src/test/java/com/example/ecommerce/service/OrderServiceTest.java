package com.example.ecommerce.service;

import com.example.ecommerce.entity.CustomerOrder;
import com.example.ecommerce.model.CreateOrderRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        when(eventPublisher.publish(eq("order-events"), any())).thenReturn(CompletableFuture.completedFuture(null));
        OrderService service = new OrderService(orderRepository, eventPublisher);

        CustomerOrder result = service.create(new CreateOrderRequest(
                "customer@example.com", "SKU-1", 2, new BigDecimal("12.50"), "CARD-1"));

        assertEquals(new BigDecimal("25.00"), result.getTotalAmount());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        ArgumentCaptor<CustomerOrder> captor = ArgumentCaptor.forClass(CustomerOrder.class);
        verify(orderRepository).save(captor.capture());
        verify(eventPublisher).publish(eq("order-events"), any());
        assertEquals("SKU-1", captor.getValue().getSku());
    }
}
