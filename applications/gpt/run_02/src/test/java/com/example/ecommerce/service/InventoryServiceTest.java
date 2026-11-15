package com.example.ecommerce.service;

import com.example.ecommerce.config.KafkaTopics;
import com.example.ecommerce.event.InventoryEvent;
import com.example.ecommerce.event.InventoryStatus;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.event.PaymentStatus;
import com.example.ecommerce.messaging.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock
    private EventPublisher publisher;

    @Test
    void publishesFailureWhenStockIsInsufficient() {
        when(publisher.publish(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        InventoryService service = new InventoryService(publisher);
        PaymentEvent payment = new PaymentEvent("event-1", "order-1", "product-1",
                101, PaymentStatus.COMPLETED, null, Instant.now());

        service.updateInventory(payment);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publish(org.mockito.ArgumentMatchers.eq(KafkaTopics.INVENTORY_EVENTS),
                org.mockito.ArgumentMatchers.eq("order-1"), captor.capture());
        InventoryEvent event = (InventoryEvent) captor.getValue();
        assertEquals(InventoryStatus.FAILED, event.status());
        assertEquals(100, service.getAvailableStock("product-1"));
    }
}
