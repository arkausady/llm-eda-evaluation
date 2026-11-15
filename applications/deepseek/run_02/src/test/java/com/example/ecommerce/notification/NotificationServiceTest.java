package com.example.ecommerce.notification;

import com.example.ecommerce.common.EventEnvelope;
import com.example.ecommerce.inventory.InventoryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void onInventoryEvent_shouldNotThrow() {
        InventoryEvent event = new InventoryEvent("O1", "UPDATED", System.currentTimeMillis());
        EventEnvelope<InventoryEvent> envelope = EventEnvelope.of("InventoryUpdated", event);

        assertDoesNotThrow(() -> notificationService.onInventoryEvent(envelope));
    }
}