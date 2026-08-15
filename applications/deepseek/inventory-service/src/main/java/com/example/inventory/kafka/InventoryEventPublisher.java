package com.example.inventory.kafka;

import com.example.common.event.InventoryFailedEvent;
import com.example.common.event.InventoryUpdatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryUpdated(InventoryUpdatedEvent event) {
        kafkaTemplate.send("inventory-events", event.getOrderId().toString(), event);
    }

    public void publishInventoryFailed(InventoryFailedEvent event) {
        kafkaTemplate.send("inventory-events", event.getOrderId().toString(), event);
    }
}
