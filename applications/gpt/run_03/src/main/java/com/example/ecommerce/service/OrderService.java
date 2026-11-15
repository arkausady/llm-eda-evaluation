package com.example.ecommerce.service;

import com.example.ecommerce.entity.CustomerOrder;
import com.example.ecommerce.model.CreateOrderRequest;
import com.example.ecommerce.model.OrderEvent;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CustomerOrder create(CreateOrderRequest request) {
        BigDecimal total = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
        CustomerOrder order = new CustomerOrder(UUID.randomUUID(), request.customerEmail(), request.sku(),
                request.quantity(), request.unitPrice(), total, request.paymentMethodId());
        orderRepository.save(order);

        OrderEvent event = OrderEvent.orderCreated(order.getId(), order.getCustomerEmail(), order.getSku(),
                order.getQuantity(), order.getTotalAmount(), order.getPaymentMethodId());

        eventPublisher.publish("order-events", event).whenComplete((result, error) -> {
            if (error != null) {
                orderRepository.findById(order.getId()).ifPresent(failedOrder -> {
                    if (!failedOrder.isTerminal()) {
                        failedOrder.updateStatus(OrderStatus.EVENT_PUBLICATION_FAILED,
                                "Unable to publish order-created event: " + error.getMessage());
                        orderRepository.save(failedOrder);
                    }
                });
            }
        });
        return order;
    }

    @Transactional(readOnly = true)
    public CustomerOrder findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> findAll() {
        return orderRepository.findAll();
    }
}
