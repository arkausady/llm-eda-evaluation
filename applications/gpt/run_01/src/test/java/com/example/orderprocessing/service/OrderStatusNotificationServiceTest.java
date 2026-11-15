package com.example.orderprocessing.service;

import com.example.orderprocessing.event.InventoryEvent;
import com.example.orderprocessing.model.CustomerOrder;
import com.example.orderprocessing.model.OrderItem;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.repository.NotificationRepository;
import com.example.orderprocessing.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderStatusNotificationServiceTest {
    @Test
    void completesOrderAndCreatesNotification() {
        var orderRepository = mock(OrderRepository.class);
        var notificationRepository = mock(NotificationRepository.class);
        var service = new OrderStatusNotificationService(orderRepository, notificationRepository);
        var orderId = UUID.randomUUID();
        var order = new CustomerOrder(orderId, "customer-1", "customer@example.com",
                new BigDecimal("10.00"), OrderStatus.PAYMENT_PROCESSED, Instant.now(),
                List.of(new OrderItem("product-1", 1, new BigDecimal("10.00"))));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        var event = new InventoryEvent(UUID.randomUUID(), orderId,
                InventoryEvent.InventoryStatus.RESERVED, List.of(), "Inventory reserved", Instant.now());

        service.handleInventory(event);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository).save(order);
        verify(notificationRepository).save(any());
    }
}
