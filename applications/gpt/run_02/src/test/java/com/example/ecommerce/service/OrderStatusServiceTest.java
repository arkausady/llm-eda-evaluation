package com.example.ecommerce.service;

import com.example.ecommerce.event.InventoryEvent;
import com.example.ecommerce.event.InventoryStatus;
import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatusServiceTest {
    @Mock
    private OrderRepository repository;

    @Test
    void completesOrderAfterInventoryUpdate() {
        CustomerOrder order = new CustomerOrder("order-1", "customer-1", "product-1", 1,
                new BigDecimal("10.00"), OrderStatus.PAYMENT_COMPLETED, Instant.now());
        when(repository.findById("order-1")).thenReturn(Optional.of(order));
        OrderStatusService service = new OrderStatusService(repository);

        service.handleInventoryStatus(new InventoryEvent("event-1", "order-1", "product-1",
                1, InventoryStatus.UPDATED, null, Instant.now()));

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(repository).save(order);
    }
}
