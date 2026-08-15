package com.ecommerce.inventory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.InventoryEventType;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.OrderItem;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final Object LOCK = new Object();

    private final InventoryRepository inventoryRepository;
    private final InventoryEventProducer inventoryEventProducer;

    public InventoryService(InventoryRepository inventoryRepository, InventoryEventProducer inventoryEventProducer) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryEventProducer = inventoryEventProducer;
    }

    public InventoryEvent reserveForPaidOrder(PaymentEvent event) {
        CustomerOrder order = event.getOrder();
        synchronized (LOCK) {
            List<String> shortages = findShortages(order);
            if (!shortages.isEmpty()) {
                String reason = "Insufficient inventory: " + String.join("; ", shortages);
                InventoryEvent failed = InventoryEvent.of(InventoryEventType.INVENTORY_FAILED, order, reason);
                inventoryEventProducer.publish(failed);
                log.warn("Inventory reservation failed for order {}: {}", order.getOrderId(), reason);
                return failed;
            }
            deduct(order);
        }
        InventoryEvent reserved = InventoryEvent.of(InventoryEventType.INVENTORY_RESERVED, order, null);
        inventoryEventProducer.publish(reserved);
        log.info("Reserved inventory for order {}", order.getOrderId());
        return reserved;
    }

    public List<InventoryItem> listStock() {
        return new ArrayList<>(inventoryRepository.findAll());
    }

    List<String> findShortages(CustomerOrder order) {
        List<String> shortages = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            InventoryItem stock = inventoryRepository.findBySku(item.getSku()).orElse(null);
            if (stock == null) {
                shortages.add(item.getSku() + " is not stocked");
            } else if (stock.getAvailableQuantity() < item.getQuantity()) {
                shortages.add(item.getSku() + " requested " + item.getQuantity()
                        + " but only " + stock.getAvailableQuantity() + " available");
            }
        }
        return shortages;
    }

    private void deduct(CustomerOrder order) {
        for (OrderItem item : order.getItems()) {
            InventoryItem stock = inventoryRepository.findBySku(item.getSku()).orElseThrow();
            stock.setAvailableQuantity(stock.getAvailableQuantity() - item.getQuantity());
            inventoryRepository.save(stock);
        }
    }
}
