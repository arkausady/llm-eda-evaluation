package com.example.ecommerce.inventory;

import com.example.ecommerce.model.InventoryUpdatedEvent;
import com.example.ecommerce.model.PaymentProcessedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void updateInventoryShouldPublishSuccessEvent() {
        PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(
                "order1", "SUCCESS", "Payment processed", new BigDecimal("200.00")
        );

        inventoryService.updateInventory(paymentEvent);

        ArgumentCaptor<InventoryUpdatedEvent> captor = ArgumentCaptor.forClass(InventoryUpdatedEvent.class);
        verify(inventoryEventPublisher, times(1)).publishInventoryUpdated(captor.capture());
        InventoryUpdatedEvent event = captor.getValue();
        assertTrue(event.isSuccess());
        assertEquals("Inventory updated successfully", event.getMessage());
    }
}
