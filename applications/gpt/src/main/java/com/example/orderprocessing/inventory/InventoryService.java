package com.example.orderprocessing.inventory;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventPublisher;
import com.example.orderprocessing.event.EventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final StockRepository stockRepository;
    private final InventoryReservationRepository reservationRepository;
    private final EventPublisher publisher;

    public InventoryService(StockRepository stockRepository,
                            InventoryReservationRepository reservationRepository,
                            EventPublisher publisher) {
        this.stockRepository = stockRepository;
        this.reservationRepository = reservationRepository;
        this.publisher = publisher;
    }

    @Transactional
    public synchronized void reserve(BusinessEvent paymentEvent) {
        if (reservationRepository.existsById(paymentEvent.orderId())) {
            publisher.publish("inventory-events", paymentEvent.next(
                    EventType.INVENTORY_RESERVED, "Inventory was already reserved"));
            return;
        }

        for (var item : paymentEvent.items()) {
            var stock = stockRepository.findById(item.sku()).orElse(null);
            if (stock == null || stock.getAvailableQuantity() < item.quantity()) {
                publisher.publish("inventory-events", paymentEvent.next(
                        EventType.INVENTORY_FAILED, "Insufficient stock for SKU " + item.sku()));
                return;
            }
        }

        for (var item : paymentEvent.items()) {
            StockEntity stock = stockRepository.findById(item.sku()).orElseThrow();
            stock.reserve(item.quantity());
        }
        reservationRepository.save(new InventoryReservationEntity(paymentEvent.orderId()));
        publisher.publish("inventory-events", paymentEvent.next(
                EventType.INVENTORY_RESERVED, "Inventory reserved successfully"));
    }
}
