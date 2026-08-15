package com.ecommerce.payment;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.ecommerce.model.PaymentRecord;

@Repository
public class PaymentRepository {

    private final ConcurrentHashMap<String, PaymentRecord> payments = new ConcurrentHashMap<>();

    public PaymentRecord save(PaymentRecord payment) {
        payments.put(payment.getPaymentId(), payment);
        return payment;
    }

    public Optional<PaymentRecord> findById(String paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    public Collection<PaymentRecord> findAll() {
        return payments.values();
    }
}
