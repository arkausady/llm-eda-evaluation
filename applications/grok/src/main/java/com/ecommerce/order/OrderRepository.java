package com.ecommerce.order;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.ecommerce.model.CustomerOrder;

@Repository
public class OrderRepository {

    private final ConcurrentHashMap<String, CustomerOrder> orders = new ConcurrentHashMap<>();

    public CustomerOrder save(CustomerOrder order) {
        orders.put(order.getOrderId(), order);
        return order;
    }

    public Optional<CustomerOrder> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public Collection<CustomerOrder> findAll() {
        return orders.values();
    }

    public void clear() {
        orders.clear();
    }
}
