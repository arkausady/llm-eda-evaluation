package com.ecommerce.orderprocessing.order;

import com.ecommerce.orderprocessing.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.dto.OrderItemRequest;
import com.ecommerce.orderprocessing.event.OrderCreatedEvent;
import com.ecommerce.orderprocessing.exception.InvalidOrderException;
import com.ecommerce.orderprocessing.exception.OrderNotFoundException;
import com.ecommerce.orderprocessing.exception.OrderProcessingException;
import com.ecommerce.orderprocessing.model.CustomerOrder;
import com.ecommerce.orderprocessing.model.OrderItem;
import com.ecommerce.orderprocessing.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    public CustomerOrder createOrder(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(this::toItem)
                .toList();
        BigDecimal total = items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("Order total must be greater than zero");
        }

        Instant now = Instant.now();
        CustomerOrder order = new CustomerOrder(
                UUID.randomUUID().toString(),
                request.customerId(),
                request.customerEmail(),
                items,
                total,
                request.paymentMethod(),
                OrderStatus.CREATED,
                null,
                now,
                now
        );
        orderRepository.save(order);
        log.info("Stored order {}", order.orderId());

        try {
            CustomerOrder pending = order.withStatus(OrderStatus.PAYMENT_PENDING, null);
            orderRepository.save(pending);
            orderEventProducer.publishOrderCreated(OrderCreatedEvent.from(pending));
            return pending;
        } catch (RuntimeException e) {
            CustomerOrder failed = order.withStatus(OrderStatus.FAILED, e.getMessage());
            orderRepository.save(failed);
            throw new OrderProcessingException("Order stored but event publishing failed", e);
        }
    }

    public CustomerOrder getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<CustomerOrder> getOrders() {
        return orderRepository.findAll();
    }

    public void updateStatus(String orderId, OrderStatus status, String reason) {
        orderRepository.findById(orderId).ifPresentOrElse(existing -> {
            CustomerOrder updated = existing.withStatus(status, reason);
            orderRepository.save(updated);
            log.info("Order {} status updated to {}", orderId, status);
        }, () -> log.warn("Cannot update missing order {}", orderId));
    }

    private OrderItem toItem(OrderItemRequest request) {
        return new OrderItem(request.productId(), request.productName(), request.quantity(), request.unitPrice());
    }
}
