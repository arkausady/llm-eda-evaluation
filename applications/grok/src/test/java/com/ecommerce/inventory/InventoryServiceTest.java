package com.ecommerce.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.InventoryEventType;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentEventType;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.PaymentRecord;
import com.ecommerce.model.PaymentStatus;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryEventProducer inventoryEventProducer;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(inventoryRepository, inventoryEventProducer);
    }

    @Test
    void reservesStockWhenAvailable() {
        InventoryItem stock = new InventoryItem("SKU-001", "Headphones", 5, new BigDecimal("79.99"));
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(Optional.of(stock));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryEvent event = inventoryService.reserveForPaidOrder(paymentEvent(1));

        assertEquals(InventoryEventType.INVENTORY_RESERVED, event.getEventType());
        assertEquals(4, stock.getAvailableQuantity());
        verify(inventoryEventProducer).publish(any(InventoryEvent.class));
    }

    @Test
    void publishesFailureWhenInsufficientStock() {
        InventoryItem stock = new InventoryItem("SKU-001", "Headphones", 1, new BigDecimal("79.99"));
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(Optional.of(stock));

        InventoryEvent event = inventoryService.reserveForPaidOrder(paymentEvent(5));

        assertEquals(InventoryEventType.INVENTORY_FAILED, event.getEventType());
        assertEquals(1, stock.getAvailableQuantity());
        ArgumentCaptor<InventoryEvent> captor = ArgumentCaptor.forClass(InventoryEvent.class);
        verify(inventoryEventProducer).publish(captor.capture());
        assertEquals(InventoryEventType.INVENTORY_FAILED, captor.getValue().getEventType());
    }

    private PaymentEvent paymentEvent(int quantity) {
        CustomerOrder order = CustomerOrder.create(
                "cust-1",
                "buyer@example.com",
                List.of(new OrderItem("SKU-001", "Headphones", quantity, new BigDecimal("79.99"))),
                "CARD"
        );
        PaymentRecord payment = PaymentRecord.of(order.getOrderId(), order.getTotalAmount(), "CARD", PaymentStatus.COMPLETED, null);
        return PaymentEvent.from(PaymentEventType.PAYMENT_COMPLETED, payment, order, null);
    }
}
