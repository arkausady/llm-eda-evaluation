package com.example.inventory.service;

import com.example.common.event.PaymentProcessedEvent;
import com.example.inventory.kafka.InventoryEventPublisher;
import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    private InventoryService inventoryService;

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryService = new InventoryService(inventoryRepository, eventPublisher);
    }

    @Test
    void updateInventory_sufficientStock_publishesUpdatedEvent() {
        UUID orderId = UUID.randomUUID();
        PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(orderId, UUID.randomUUID(), true, Instant.now());
        UUID productId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        InventoryItem item = new InventoryItem();
        item.setProductId(productId);
        item.setQuantity(10);
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(item));

        inventoryService.updateInventory(paymentEvent);

        verify(eventPublisher).publishInventoryUpdated(any());
        verify(eventPublisher, never()).publishInventoryFailed(any());
    }
}
