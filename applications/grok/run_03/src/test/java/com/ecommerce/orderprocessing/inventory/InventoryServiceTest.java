package com.ecommerce.orderprocessing.inventory;

import com.ecommerce.orderprocessing.event.InventoryEvent;
import com.ecommerce.orderprocessing.event.PaymentEvent;
import com.ecommerce.orderprocessing.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryServiceTest {

    private InventoryRepository repository;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        repository = new InventoryRepository();
        inventoryService = new InventoryService(repository);
    }

    @Test
    void reservesAvailableStock() {
        InventoryEvent event = inventoryService.process(payment(List.of(
                new OrderItem("PROD-001", "Laptop", 2, new BigDecimal("999.99"))
        )));
        assertTrue(event.success());
        assertEquals(InventoryEvent.UPDATED, event.eventType());
        assertEquals(8, repository.findById("PROD-001").orElseThrow().availableQuantity());
    }

    @Test
    void failsWhenStockInsufficientWithoutPartialCommit() {
        InventoryEvent event = inventoryService.process(payment(List.of(
                new OrderItem("PROD-001", "Laptop", 2, new BigDecimal("999.99")),
                new OrderItem("PROD-002", "Mouse", 500, new BigDecimal("29.99"))
        )));
        assertFalse(event.success());
        assertEquals(InventoryEvent.FAILED, event.eventType());
        assertEquals(10, repository.findById("PROD-001").orElseThrow().availableQuantity());
        assertEquals(50, repository.findById("PROD-002").orElseThrow().availableQuantity());
    }

    @Test
    void failsForUnknownProduct() {
        InventoryEvent event = inventoryService.process(payment(List.of(
                new OrderItem("UNKNOWN", "Ghost", 1, new BigDecimal("1.00"))
        )));
        assertFalse(event.success());
    }

    private PaymentEvent payment(List<OrderItem> items) {
        return new PaymentEvent(
                "pay-1",
                PaymentEvent.PROCESSED,
                Instant.now(),
                "order-1",
                "CUST-1",
                "alice@example.com",
                items,
                new BigDecimal("10.00"),
                "CREDIT_CARD",
                true,
                "ok"
        );
    }
}
