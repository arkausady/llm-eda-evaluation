package com.example.ecommerce.order;

import com.example.ecommerce.model.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository, OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    public Order createOrder(OrderController.OrderRequest request) {
        Order order = new Order(
                request.getCustomerId(),
                request.getProductId(),
                request.getQuantity(),
                request.getAmount()
        );
        orderRepository.save(order);
        orderEventPublisher.publishOrderCreated(order);
        return order;
    }
}
