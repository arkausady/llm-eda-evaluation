package com.example.ecommerce.order;

import com.example.ecommerce.common.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();

    @Value("${kafka.topics.order-events}")
    private String orderEventsTopic;

    public Order createOrder(String customerId, List<OrderItem> items) {
        String orderId = UUID.randomUUID().toString();
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = new Order(orderId, customerId, items, total, "CREATED");
        orderStore.put(orderId, order);

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, customerId, items, total, System.currentTimeMillis());
        EventEnvelope<OrderCreatedEvent> envelope = EventEnvelope.of("OrderCreated", event);
        kafkaTemplate.send(orderEventsTopic, orderId, envelope);

        return order;
    }

    public Order getOrder(String orderId) {
        return orderStore.get(orderId);
    }
}