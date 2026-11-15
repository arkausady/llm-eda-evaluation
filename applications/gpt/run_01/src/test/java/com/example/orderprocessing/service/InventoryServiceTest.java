package com.example.orderprocessing.service;

import com.example.orderprocessing.event.InventoryEvent;
import com.example.orderprocessing.event.OrderItemData;
import com.example.orderprocessing.event.PaymentEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InventoryServiceTest {
    @Test
    void reservesAvailableInventory() {
        var publisher = mock(EventPublisher.class);
        var service = new InventoryService(publisher);
        var event = paymentEvent("product-1", 2);

        service.updateInventory(event);

        var captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publish(org.mockito.ArgumentMatchers.eq("inventory-events"),
                org.mockito.ArgumentMatchers.eq(event.orderId().toString()), captor.capture());
        assertEquals(InventoryEvent.InventoryStatus.RESERVED,
                ((InventoryEvent) captor.getValue()).status());
    }

    @Test
    void reportsUnavailableInventory() {
        var publisher = mock(EventPublisher.class);
        var service = new InventoryService(publisher);
        var event = paymentEvent("OUT-OF-STOCK-1", 1);

        service.updateInventory(event);

        var captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publish(org.mockito.ArgumentMatchers.eq("inventory-events"),
                org.mockito.ArgumentMatchers.eq(event.orderId().toString()), captor.capture());
        assertEquals(InventoryEvent.InventoryStatus.FAILED,
                ((InventoryEvent) captor.getValue()).status());
    }

    private PaymentEvent paymentEvent(String productId, int quantity) {
        return new PaymentEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                PaymentEvent.PaymentStatus.SUCCEEDED, new BigDecimal("10.00"),
                List.of(new OrderItemData(productId, quantity, new BigDecimal("10.00"))),
                "Payment succeeded", Instant.now());
    }
}
