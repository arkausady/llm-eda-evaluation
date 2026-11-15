package com.ecommerce.orderprocessing.order;

import com.ecommerce.orderprocessing.model.CustomerOrder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {

    private final ConcurrentHashMap<String, CustomerOrder> orders = new ConcurrentHashMap<>();

    public CustomerOrder save(CustomerOrder order) {
        orders.put(order.orderId(), order);
        return order;
    }

    public Optional<CustomerOrder> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public List<CustomerOrder> findAll() {
        return List.copyOf(orders.values());
    }
}
