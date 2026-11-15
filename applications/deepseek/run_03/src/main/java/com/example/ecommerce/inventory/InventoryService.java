package com.example.ecommerce.inventory;

import com.example.ecommerce.model.InventoryUpdatedEvent;
import com.example.ecommerce.model.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryEventPublisher inventoryEventPublisher;

    public InventoryService(InventoryEventPublisher inventoryEventPublisher) {
        this.inventoryEventPublisher = inventoryEventPublisher;
    }

    public void updateInventory(PaymentProcessedEvent paymentEvent) {
        try {
            // Simulate inventory update
            // In real app, this would decrement stock for the product
            log.info("Updating inventory for order {}: product {}, quantity {}",
                    paymentEvent.getOrderId(), "product", 1);
            InventoryUpdatedEvent event = new InventoryUpdatedEvent(
                    paymentEvent.getOrderId(),
                    "product", // placeholder, should be from order context
                    1,
                    true,
                    "Inventory updated successfully"
            );
            inventoryEventPublisher.publishInventoryUpdated(event);
        } catch (Exception e) {
            log.error("Inventory update failed for order {}", paymentEvent.getOrderId(), e);
            InventoryUpdatedEvent failureEvent = new InventoryUpdatedEvent(
                    paymentEvent.getOrderId(),
                    "product",
                    0,
                    false,
                    "Inventory update failed: " + e.getMessage()
            );
            inventoryEventPublisher.publishInventoryUpdated(failureEvent);
        }
    }
}
