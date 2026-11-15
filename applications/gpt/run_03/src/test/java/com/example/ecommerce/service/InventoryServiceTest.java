package com.example.ecommerce.service;

import com.example.ecommerce.entity.InventoryItem;
import com.example.ecommerce.model.EventType;
import com.example.ecommerce.model.OrderEvent;
import com.example.ecommerce.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Test
    void reservesInventoryAndPublishesSuccess() {
        InventoryItem item = new InventoryItem("SKU-1", 5);
        when(inventoryRepository.findById("SKU-1")).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventPublisher.publish(eq("inventory-events"), any())).thenReturn(CompletableFuture.completedFuture(null));
        InventoryService service = new InventoryService(inventoryRepository, eventPublisher);
        OrderEvent event = new OrderEvent(UUID.randomUUID(), UUID.randomUUID(), EventType.PAYMENT_COMPLETED,
                "customer@example.com", "SKU-1", 2, new BigDecimal("20.00"),
                "CARD-1", null, Instant.now());

        service.updateInventory(event);

        assertEquals(3, item.getAvailableQuantity());
        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(eventPublisher).publish(eq("inventory-events"), captor.capture());
        assertEquals(EventType.INVENTORY_RESERVED, captor.getValue().type());
    }
}
