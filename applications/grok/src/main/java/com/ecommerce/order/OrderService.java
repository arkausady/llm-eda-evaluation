package com.ecommerce.order;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.OrderEventType;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.inventory.InventoryRepository;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository,
                        InventoryRepository inventoryRepository,
                        OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderEventProducer = orderEventProducer;
    }

    public CustomerOrder createOrder(CreateOrderRequest request) {
        List<OrderItem> items = resolveItems(request.getItems());
        CustomerOrder order = CustomerOrder.create(
                request.getCustomerId(),
                request.getCustomerEmail(),
                items,
                request.getPaymentMethod()
        );
        order.mark(OrderStatus.PAYMENT_PENDING, null);
        orderRepository.save(order);
        orderEventProducer.publish(OrderEvent.of(OrderEventType.ORDER_CREATED, order, null));
        log.info("Stored order {} and published ORDER_CREATED", order.getOrderId());
        return order;
    }

    public CustomerOrder getOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<CustomerOrder> getOrders() {
        return new ArrayList<>(orderRepository.findAll());
    }

    public void handlePaymentEvent(PaymentEvent event) {
        CustomerOrder order = getOrder(event.getOrderId());
        switch (event.getEventType()) {
            case PAYMENT_COMPLETED -> order.mark(OrderStatus.PAYMENT_COMPLETED, null);
            case PAYMENT_FAILED -> {
                order.mark(OrderStatus.PAYMENT_FAILED, event.getReason());
                orderEventProducer.publish(OrderEvent.of(OrderEventType.ORDER_FAILED, order, event.getReason()));
            }
            case PAYMENT_REFUNDED -> {
                order.mark(OrderStatus.CANCELLED, event.getReason());
                orderEventProducer.publish(OrderEvent.of(OrderEventType.ORDER_CANCELLED, order, event.getReason()));
            }
        }
        orderRepository.save(order);
    }

    public void handleInventoryEvent(InventoryEvent event) {
        CustomerOrder order = getOrder(event.getOrderId());
        switch (event.getEventType()) {
            case INVENTORY_RESERVED -> {
                order.mark(OrderStatus.COMPLETED, null);
                orderRepository.save(order);
                orderEventProducer.publish(OrderEvent.of(OrderEventType.ORDER_COMPLETED, order, null));
            }
            case INVENTORY_FAILED -> {
                order.mark(OrderStatus.INVENTORY_FAILED, event.getReason());
                orderRepository.save(order);
            }
        }
    }

    public void markProcessingError(String orderId, String reason) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.mark(OrderStatus.PROCESSING_ERROR, reason);
            orderRepository.save(order);
        });
    }

    private List<OrderItem> resolveItems(List<OrderItemRequest> requestedItems) {
        List<OrderItem> resolved = new ArrayList<>();
        for (OrderItemRequest requested : requestedItems) {
            InventoryItem catalogItem = inventoryRepository.findBySku(requested.getSku())
                    .orElseThrow(() -> new InvalidOrderException("Unknown SKU: " + requested.getSku()));
            resolved.add(new OrderItem(
                    catalogItem.getSku(),
                    catalogItem.getName(),
                    requested.getQuantity(),
                    catalogItem.getUnitPrice()
            ));
        }
        return resolved;
    }
}
