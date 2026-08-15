package com.example.ecommerce.consumer;

import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);
    private final InventoryService inventoryService;

    public InventoryConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "inventory-group")
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Inventory Service received PaymentEvent for orderId: {}", paymentEvent.orderId());
        if ("SUCCESS".equals(paymentEvent.status())) {
            inventoryService.processInventory(paymentEvent, "default-product", 1);
        }
    }
}