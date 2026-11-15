package com.ecommerce.payment.repository;

import com.ecommerce.payment.domain.PaymentRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PaymentRepository {

    private final ConcurrentHashMap<String, PaymentRecord> payments = new ConcurrentHashMap<>();

    public PaymentRecord save(PaymentRecord record) {
        payments.put(record.getOrderId(), record);
        return record;
    }

    public Optional<PaymentRecord> findByOrderId(String orderId) {
        return Optional.ofNullable(payments.get(orderId));
    }

    public List<PaymentRecord> findAll() {
        return new ArrayList<>(payments.values());
    }
}
