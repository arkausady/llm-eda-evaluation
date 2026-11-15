package com.ecommerce.orderprocessing.inventory;

import com.ecommerce.orderprocessing.event.InventoryEvent;
import com.ecommerce.orderprocessing.event.PaymentEvent;
import com.ecommerce.orderprocessing.model.InventoryItem;
import com.ecommerce.orderprocessing.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public synchronized InventoryEvent process(PaymentEvent paymentEvent) {
        List<String> problems = new ArrayList<>();
        for (OrderItem item : paymentEvent.items()) {
            InventoryItem stock = inventoryRepository.findById(item.productId()).orElse(null);
            if (stock == null) {
                problems.add("Unknown product " + item.productId());
            } else if (stock.availableQuantity() < item.quantity()) {
                problems.add("Insufficient stock for " + item.productId()
                        + " (requested " + item.quantity() + ", available " + stock.availableQuantity() + ")");
            }
        }

        if (!problems.isEmpty()) {
            String message = String.join("; ", problems);
            log.warn("Inventory update failed for order {}: {}", paymentEvent.orderId(), message);
            return new InventoryEvent(
                    UUID.randomUUID().toString(),
                    InventoryEvent.FAILED,
                    Instant.now(),
                    paymentEvent.orderId(),
                    paymentEvent.customerId(),
                    paymentEvent.customerEmail(),
                    paymentEvent.items(),
                    false,
                    message
            );
        }

        for (OrderItem item : paymentEvent.items()) {
            InventoryItem stock = inventoryRepository.findById(item.productId()).orElseThrow();
            inventoryRepository.save(stock.decrement(item.quantity()));
        }
        log.info("Inventory reserved for order {}", paymentEvent.orderId());
        return new InventoryEvent(
                UUID.randomUUID().toString(),
                InventoryEvent.UPDATED,
                Instant.now(),
                paymentEvent.orderId(),
                paymentEvent.customerId(),
                paymentEvent.customerEmail(),
                paymentEvent.items(),
                true,
                "Inventory reserved successfully"
        );
    }

    public List<InventoryItem> getStock() {
        return inventoryRepository.findAll();
    }
}
