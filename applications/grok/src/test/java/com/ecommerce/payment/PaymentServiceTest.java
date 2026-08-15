package com.ecommerce.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.OrderEventType;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentEventType;
import com.ecommerce.exception.TransientProcessingException;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.PaymentRecord;
import com.ecommerce.model.PaymentStatus;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, paymentEventProducer);
        when(paymentRepository.save(any(PaymentRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void successfulPaymentPublishesCompletedEvent() {
        CustomerOrder order = order("CARD");
        PaymentRecord payment = paymentService.processOrderCreated(OrderEvent.of(OrderEventType.ORDER_CREATED, order, null));

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventProducer).publish(captor.capture());
        assertEquals(PaymentEventType.PAYMENT_COMPLETED, captor.getValue().getEventType());
    }

    @Test
    void failPaymentMethodPublishesFailedEvent() {
        CustomerOrder order = order(PaymentService.FAIL_METHOD);
        PaymentRecord payment = paymentService.processOrderCreated(OrderEvent.of(OrderEventType.ORDER_CREATED, order, null));

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventProducer).publish(captor.capture());
        assertEquals(PaymentEventType.PAYMENT_FAILED, captor.getValue().getEventType());
    }

    @Test
    void transientMethodThrowsRetryableException() {
        CustomerOrder order = order(PaymentService.TRANSIENT_METHOD);
        assertThrows(TransientProcessingException.class,
                () -> paymentService.processOrderCreated(OrderEvent.of(OrderEventType.ORDER_CREATED, order, null)));
    }

    @Test
    void refundPublishesRefundedEvent() {
        CustomerOrder order = order("CARD");
        paymentService.refund(order, "out of stock");

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventProducer).publish(captor.capture());
        assertEquals(PaymentEventType.PAYMENT_REFUNDED, captor.getValue().getEventType());
    }

    private CustomerOrder order(String method) {
        return CustomerOrder.create(
                "cust-1",
                "buyer@example.com",
                List.of(new OrderItem("SKU-001", "Headphones", 1, new BigDecimal("79.99"))),
                method
        );
    }
}
