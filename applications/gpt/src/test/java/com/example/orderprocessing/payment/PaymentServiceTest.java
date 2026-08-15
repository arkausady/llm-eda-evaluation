package com.example.orderprocessing.payment;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository repository;
    @Mock
    private EventPublisher publisher;

    @Test
    void publishesFailureForDeclinedPayment() {
        UUID orderId = UUID.randomUUID();
        BusinessEvent event = BusinessEvent.orderCreated(orderId, new BigDecimal("10.00"),
                List.of(new OrderItem("SKU-100", 1)), "FAIL-CARD");
        when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());
        PaymentService service = new PaymentService(repository, publisher);

        service.process(event);

        verify(repository).save(any(PaymentEntity.class));
        ArgumentCaptor<BusinessEvent> result = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(publisher).publish(eq("payment-events"), result.capture());
        assertEquals(EventType.PAYMENT_FAILED, result.getValue().type());
    }
}
