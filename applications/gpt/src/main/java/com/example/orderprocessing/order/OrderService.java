package com.example.orderprocessing.order;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventPublisher;
import com.example.orderprocessing.event.EventType;
import com.example.orderprocessing.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository repository;
    private final EventPublisher publisher;

    public OrderService(OrderRepository repository, EventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        OrderEntity order = repository.save(new OrderEntity(
                request.customerId(), request.amount(), request.items()));
        publisher.publish("order-events", BusinessEvent.orderCreated(
                order.getId(), order.getAmount(), order.toOrderItems(), request.paymentToken()));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse find(UUID orderId) {
        return OrderResponse.from(required(orderId));
    }

    @Transactional
    public void handlePaymentEvent(BusinessEvent event) {
        OrderEntity order = required(event.orderId());
        if (event.type() == EventType.PAYMENT_SUCCEEDED && order.getStatus() == OrderStatus.CREATED) {
            order.updateStatus(OrderStatus.PAYMENT_COMPLETED, event.message());
        } else if (event.type() == EventType.PAYMENT_FAILED && order.getStatus() != OrderStatus.FAILED) {
            failAndPublish(order, event, "Payment failed: " + event.message());
        }
    }

    @Transactional
    public void handleInventoryEvent(BusinessEvent event) {
        OrderEntity order = required(event.orderId());
        if (event.type() == EventType.INVENTORY_RESERVED && order.getStatus() != OrderStatus.COMPLETED) {
            order.updateStatus(OrderStatus.COMPLETED, "Order processed successfully");
            publisher.publish("order-events", event.next(EventType.ORDER_COMPLETED,
                    "Order processed successfully"));
        } else if (event.type() == EventType.INVENTORY_FAILED && order.getStatus() != OrderStatus.FAILED) {
            failAndPublish(order, event, "Inventory failed: " + event.message());
        }
    }

    private void failAndPublish(OrderEntity order, BusinessEvent source, String message) {
        order.updateStatus(OrderStatus.FAILED, message);
        publisher.publish("order-events", source.next(EventType.ORDER_FAILED, message));
    }

    private OrderEntity required(UUID orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}
