package com.example.orderprocessing.repository;

import com.example.orderprocessing.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
}
