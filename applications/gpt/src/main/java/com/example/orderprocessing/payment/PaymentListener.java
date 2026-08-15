package com.example.orderprocessing.payment;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentListener {
    private final PaymentService service;

    public PaymentListener(PaymentService service) {
        this.service = service;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-processing")
    public void receive(BusinessEvent event) {
        if (event.type() == EventType.ORDER_CREATED) {
            service.process(event);
        }
    }
}
