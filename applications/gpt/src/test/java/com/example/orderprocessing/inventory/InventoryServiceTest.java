package com.example.orderprocessing.inventory;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventPublisher;
import com.example.orderprocessing.event.EventType;
import com.example.orderprocessing.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock
    private StockRepository stockRepository;
    @Mock
    private InventoryReservationRepository reservationRepository;
    @Mock
    private EventPublisher publisher;

    @Test
    void reservesAvailableInventory() {
        UUID orderId = UUID.randomUUID();
        StockEntity stock = new StockEntity("SKU-100", 10);
        when(reservationRepository.existsById(orderId)).thenReturn(false);
        when(stockRepository.findById("SKU-100")).thenReturn(Optional.of(stock));
        InventoryService service = new InventoryService(stockRepository, reservationRepository, publisher);
        BusinessEvent event = new BusinessEvent(UUID.randomUUID(), orderId,
                EventType.PAYMENT_SUCCEEDED, Instant.now(), new BigDecimal("25.00"),
                List.of(new OrderItem("SKU-100", 3)), null, "Paid");

        service.reserve(event);

        assertEquals(7, stock.getAvailableQuantity());
        verify(reservationRepository).save(any(InventoryReservationEntity.class));
        ArgumentCaptor<BusinessEvent> result = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(publisher).publish(eq("inventory-events"), result.capture());
        assertEquals(EventType.INVENTORY_RESERVED, result.getValue().type());
    }
}
