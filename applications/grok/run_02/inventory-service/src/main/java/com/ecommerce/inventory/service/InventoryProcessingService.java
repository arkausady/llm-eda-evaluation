package com.ecommerce.inventory.service;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.event.PaymentStatus;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.inventory.producer.InventoryEventProducer;
import com.ecommerce.inventory.stock.InventoryStock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryProcessingService {

    private static final Logger log = LoggerFactory.getLogger(InventoryProcessingService.class);

    private final InventoryStock inventoryStock;
    private final InventoryEventProducer inventoryEventProducer;

    public InventoryProcessingService(InventoryStock inventoryStock, InventoryEventProducer inventoryEventProducer) {
        this.inventoryStock = inventoryStock;
        this.inventoryEventProducer = inventoryEventProducer;
    }

    public InventoryEvent process(PaymentEvent paymentEvent) {
        if (paymentEvent == null || paymentEvent.orderId() == null) {
            throw new ProcessingException("Invalid payment event");
        }
        if (paymentEvent.status() != PaymentStatus.SUCCESS) {
            log.info("Skipping inventory update for unsuccessful payment orderId={}", paymentEvent.orderId());
            return null;
        }
        if (paymentEvent.items() == null || paymentEvent.items().isEmpty()) {
            InventoryEvent failed = InventoryEvent.failed(paymentEvent, "Order contains no items");
            inventoryEventProducer.publish(failed);
            return failed;
        }

        boolean reserved = inventoryStock.reserve(paymentEvent.items());
        InventoryEvent event = reserved
                ? InventoryEvent.reserved(paymentEvent)
                : InventoryEvent.failed(paymentEvent, "Insufficient stock");
        inventoryEventProducer.publish(event);
        log.info("Inventory processed for order {} status={}", paymentEvent.orderId(), event.status());
        return event;
    }
}
