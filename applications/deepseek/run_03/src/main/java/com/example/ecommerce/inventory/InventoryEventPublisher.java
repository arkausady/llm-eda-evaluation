package com.example.ecommerce.inventory;

import com.example.ecommerce.model.InventoryUpdatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryUpdated(InventoryUpdatedEvent event) {
        kafkaTemplate.send("inventory-events", event.getOrderId(), event);
    }
}
