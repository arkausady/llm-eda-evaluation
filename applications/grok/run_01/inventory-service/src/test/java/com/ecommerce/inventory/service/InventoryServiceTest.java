package com.ecommerce.inventory.service;

import com.ecommerce.inventory.repository.StockRepository;
import com.ecommerce.shared.event.EventTypes;
import com.ecommerce.shared.event.InventoryEvent;
import com.ecommerce.shared.event.PaymentEvent;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.ecommerce.shared.model.OrderItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private InventoryService inventoryService;
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        stockRepository = new StockRepository();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        inventoryService = new InventoryService(stockRepository, kafkaTemplate, objectMapper);
    }

    @Test
    void reservesStockWhenAvailable() {
        PaymentEvent payment = PaymentEvent.completed(
                "order-1",
                "cust-1",
                List.of(new OrderItem("PROD-3", "Keyboard", 2, new BigDecimal("20.00"))),
                new BigDecimal("40.00"),
                "txn-1"
        );

        InventoryEvent result = inventoryService.reserveForPayment(payment);

        assertEquals(EventTypes.INVENTORY_RESERVED, result.eventType());
        assertEquals(8, stockRepository.findByProductId("PROD-3").orElseThrow().getQuantity());
        verify(kafkaTemplate).send(eq(KafkaTopics.INVENTORY_EVENTS), eq("order-1"), anyString());
    }

    @Test
    void failsWhenInsufficientStock() {
        PaymentEvent payment = PaymentEvent.completed(
                "order-2",
                "cust-1",
                List.of(new OrderItem("PROD-3", "Keyboard", 1000, new BigDecimal("20.00"))),
                new BigDecimal("20000.00"),
                "txn-2"
        );

        InventoryEvent result = inventoryService.reserveForPayment(payment);

        assertEquals(EventTypes.INVENTORY_FAILED, result.eventType());
        assertTrue(result.reason().contains("Insufficient stock"));
        assertEquals(10, stockRepository.findByProductId("PROD-3").orElseThrow().getQuantity());
    }
}
