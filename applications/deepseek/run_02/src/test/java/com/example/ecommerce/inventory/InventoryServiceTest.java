package com.example.ecommerce.inventory;

import com.example.ecommerce.common.EventEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void updateInventory_shouldPublishInventoryEvent() {
        ReflectionTestUtils.setField(inventoryService, "inventoryEventsTopic", "inventory-events");

        inventoryService.updateInventory("O1");

        ArgumentCaptor<EventEnvelope> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(eq("inventory-events"), eq("O1"), captor.capture());

        EventEnvelope envelope = captor.getValue();
        assertEquals("InventoryUpdated", envelope.getEventType());
        assertTrue(envelope.getPayload() instanceof InventoryEvent);
        InventoryEvent event = (InventoryEvent) envelope.getPayload();
        assertEquals("O1", event.getOrderId());
        assertEquals("UPDATED", event.getInventoryStatus());
    }
}