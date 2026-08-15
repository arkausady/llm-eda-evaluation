package com.ecommerce.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.InventoryEventType;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.OrderEventType;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentEventType;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.inventory.InventoryRepository;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentRecord;
import com.ecommerce.model.PaymentStatus;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, inventoryRepository, orderEventProducer);
    }

    @Test
    void createOrderStoresAndPublishesOrderCreated() {
        when(inventoryRepository.findBySku("SKU-001"))
                .thenReturn(Optional.of(new InventoryItem("SKU-001", "Headphones", 10, new BigDecimal("79.99"))));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder created = orderService.createOrder(request("cust-1", "SKU-001", 2));

        assertEquals(OrderStatus.PAYMENT_PENDING, created.getStatus());
        assertEquals(new BigDecimal("159.98"), created.getTotalAmount());
        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(orderEventProducer).publish(captor.capture());
        assertEquals(OrderEventType.ORDER_CREATED, captor.getValue().getEventType());
        assertNotNull(created.getOrderId());
    }

    @Test
    void createOrderRejectsUnknownSku() {
        when(inventoryRepository.findBySku("MISSING")).thenReturn(Optional.empty());
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request("cust-1", "MISSING", 1)));
        verify(orderEventProducer, never()).publish(any());
    }

    @Test
    void getOrderThrowsWhenMissing() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder("missing"));
    }

    @Test
    void paymentFailedMarksOrderAndPublishesFailure() {
        CustomerOrder order = existingOrder();
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRecord payment = PaymentRecord.of(order.getOrderId(), order.getTotalAmount(), "CARD", PaymentStatus.FAILED, "declined");
        PaymentEvent event = PaymentEvent.from(PaymentEventType.PAYMENT_FAILED, payment, order, "declined");

        orderService.handlePaymentEvent(event);

        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(orderEventProducer).publish(captor.capture());
        assertEquals(OrderEventType.ORDER_FAILED, captor.getValue().getEventType());
    }

    @Test
    void inventoryReservedCompletesOrder() {
        CustomerOrder order = existingOrder();
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.handleInventoryEvent(InventoryEvent.of(InventoryEventType.INVENTORY_RESERVED, order, null));

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(orderEventProducer).publish(captor.capture());
        assertEquals(OrderEventType.ORDER_COMPLETED, captor.getValue().getEventType());
    }

    private CreateOrderRequest request(String customerId, String sku, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setSku(sku);
        item.setQuantity(quantity);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setCustomerEmail("buyer@example.com");
        request.setItems(List.of(item));
        request.setPaymentMethod("CARD");
        return request;
    }

    private CustomerOrder existingOrder() {
        return CustomerOrder.create(
                "cust-1",
                "buyer@example.com",
                List.of(new OrderItem("SKU-001", "Headphones", 1, new BigDecimal("79.99"))),
                "CARD"
        );
    }
}
