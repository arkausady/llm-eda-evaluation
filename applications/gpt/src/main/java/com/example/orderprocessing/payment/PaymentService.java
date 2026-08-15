package com.example.orderprocessing.payment;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventPublisher;
import com.example.orderprocessing.event.EventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository repository;
    private final EventPublisher publisher;

    public PaymentService(PaymentRepository repository, EventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public void process(BusinessEvent orderEvent) {
        var existing = repository.findByOrderId(orderEvent.orderId());
        if (existing.isPresent()) {
            PaymentEntity payment = existing.get();
            EventType type = payment.getStatus() == PaymentStatus.SUCCEEDED
                    ? EventType.PAYMENT_SUCCEEDED : EventType.PAYMENT_FAILED;
            publisher.publish("payment-events", orderEvent.next(type, payment.getMessage()));
            return;
        }

        boolean declined = orderEvent.paymentToken() == null
                || orderEvent.paymentToken().toUpperCase().startsWith("FAIL");
        PaymentStatus status = declined ? PaymentStatus.FAILED : PaymentStatus.SUCCEEDED;
        String message = declined ? "Payment was declined" : "Payment processed successfully";
        repository.save(new PaymentEntity(orderEvent.orderId(), orderEvent.amount(), status, message));
        publisher.publish("payment-events", orderEvent.next(
                declined ? EventType.PAYMENT_FAILED : EventType.PAYMENT_SUCCEEDED, message));
    }
}
