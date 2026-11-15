package com.example.ecommerce.service;

import com.example.ecommerce.model.EventType;
import com.example.ecommerce.model.OrderEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    @Test
    void publishesPaymentFailureWhenDeclined() {
        when(eventPublisher.publish(eq("payment-events"), any())).thenReturn(CompletableFuture.completedFuture(null));
        PaymentService service = new PaymentService(eventPublisher);
        OrderEvent event = new OrderEvent(UUID.randomUUID(), UUID.randomUUID(), EventType.ORDER_CREATED,
                "customer@example.com", "SKU-1", 1, new BigDecimal("10.00"),
                "DECLINE-TEST", null, Instant.now());

        service.processPayment(event);

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(eventPublisher).publish(eq("payment-events"), captor.capture());
        assertEquals(EventType.PAYMENT_FAILED, captor.getValue().type());
    }
}
