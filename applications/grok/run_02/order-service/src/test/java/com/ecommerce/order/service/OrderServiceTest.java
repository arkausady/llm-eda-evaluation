package com.ecommerce.order.service;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.event.PaymentStatus;
import com.ecommerce.common.model.Order;
import com.ecommerce.common.model.OrderItem;
import com.ecommerce.common.model.OrderStatus;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.producer.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderEventProducer);
    }

    @Test
    void createStoresOrderAndPublishesEvent() {
        CreateOrderRequest request = new CreateOrderRequest(
                "cust-1",
                List.of(new OrderItemRequest("SKU-001", 2, new BigDecimal("10.00")))
        );
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order created = orderService.create(request);

        assertThat(created.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(created.getTotalAmount()).isEqualByComparingTo("20.00");
        verify(orderRepository).save(created);

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderEventProducer).publish(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(created.getOrderId());
        assertThat(captor.getValue().customerId()).isEqualTo("cust-1");
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getById("missing"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void applyPaymentSuccessUpdatesStatus() {
        Order order = Order.create("cust-1", List.of(new OrderItem("SKU-001", 1, new BigDecimal("5.00"))));
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        PaymentEvent event = new PaymentEvent(
                "evt-1",
                order.getOrderId(),
                "cust-1",
                order.getItems(),
                order.getTotalAmount(),
                PaymentStatus.SUCCESS,
                null,
                Instant.now()
        );

        orderService.applyPayment(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
    }

    @Test
    void applyInventoryFailureUpdatesStatus() {
        Order order = Order.create("cust-1", List.of(new OrderItem("SKU-001", 1, new BigDecimal("5.00"))));
        order.markPaymentSucceeded();
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        InventoryEvent event = InventoryEvent.failed(
                PaymentEvent.success(OrderCreatedEvent.from(order)),
                "Insufficient stock"
        );

        orderService.applyInventory(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.INVENTORY_FAILED);
        assertThat(order.getFailureReason()).isEqualTo("Insufficient stock");
    }
}
