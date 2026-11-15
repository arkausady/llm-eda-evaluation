package com.example.ecommerce.repository;

import com.example.ecommerce.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
}
