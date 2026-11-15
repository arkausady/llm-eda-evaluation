package com.ecommerce.order.service;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.event.InventoryStatus;
import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.event.PaymentStatus;
import com.ecommerce.common.model.Order;
import com.ecommerce.common.model.OrderItem;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.producer.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    public Order create(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(item.productId(), item.quantity(), item.unitPrice()))
                .toList();
        Order order = Order.create(request.customerId(), items);
        orderRepository.save(order);
        orderEventProducer.publish(OrderCreatedEvent.from(order));
        log.info("Stored order {} with status {}", order.getOrderId(), order.getStatus());
        return order;
    }

    public Order getById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public void applyPayment(PaymentEvent event) {
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if (event.status() == PaymentStatus.SUCCESS) {
                order.markPaymentSucceeded();
            } else {
                order.markPaymentFailed(event.reason());
            }
            orderRepository.save(order);
            log.info("Updated order {} after payment event status={}", order.getOrderId(), order.getStatus());
        }, () -> log.warn("Payment event for unknown order {}", event.orderId()));
    }

    public void applyInventory(InventoryEvent event) {
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if (event.status() == InventoryStatus.RESERVED) {
                order.markCompleted();
            } else {
                order.markInventoryFailed(event.reason());
            }
            orderRepository.save(order);
            log.info("Updated order {} after inventory event status={}", order.getOrderId(), order.getStatus());
        }, () -> log.warn("Inventory event for unknown order {}", event.orderId()));
    }
}
