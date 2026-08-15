package com.example.orderprocessing.inventory;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryListener {
    private final InventoryService service;

    public InventoryListener(InventoryService service) {
        this.service = service;
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-processing")
    public void receive(BusinessEvent event) {
        if (event.type() == EventType.PAYMENT_SUCCEEDED) {
            service.reserve(event);
        }
    }
}
