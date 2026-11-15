package com.ecommerce.inventory.service;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.event.InventoryStatus;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.event.PaymentStatus;
import com.ecommerce.common.model.OrderItem;
import com.ecommerce.inventory.producer.InventoryEventProducer;
import com.ecommerce.inventory.stock.InventoryStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryProcessingServiceTest {

    @Mock
    private InventoryEventProducer inventoryEventProducer;

    private InventoryProcessingService service;

    @BeforeEach
    void setUp() {
        service = new InventoryProcessingService(new InventoryStock(), inventoryEventProducer);
    }

    @Test
    void reservesStockOnSuccessfulPayment() {
        PaymentEvent payment = payment(PaymentStatus.SUCCESS, "SKU-001", 2);

        InventoryEvent result = service.process(payment);

        assertThat(result.status()).isEqualTo(InventoryStatus.RESERVED);
        verify(inventoryEventProducer).publish(result);
    }

    @Test
    void failsWhenStockInsufficient() {
        PaymentEvent payment = payment(PaymentStatus.SUCCESS, "SKU-003", 1);

        InventoryEvent result = service.process(payment);

        assertThat(result.status()).isEqualTo(InventoryStatus.FAILED);
        assertThat(result.reason()).contains("Insufficient stock");
        verify(inventoryEventProducer).publish(result);
    }

    @Test
    void skipsUnsuccessfulPayment() {
        PaymentEvent payment = payment(PaymentStatus.FAILED, "SKU-001", 1);

        InventoryEvent result = service.process(payment);

        assertThat(result).isNull();
        verify(inventoryEventProducer, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    private PaymentEvent payment(PaymentStatus status, String sku, int quantity) {
        return new PaymentEvent(
                "evt-1",
                "order-1",
                "cust-1",
                List.of(new OrderItem(sku, quantity, new BigDecimal("10.00"))),
                new BigDecimal("10.00"),
                status,
                status == PaymentStatus.FAILED ? "declined" : null,
                Instant.now()
        );
    }
}
