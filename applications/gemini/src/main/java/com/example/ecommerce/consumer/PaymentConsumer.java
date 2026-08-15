package com.example.ecommerce.consumer;

import com.example.ecommerce.event.OrderEvent;
import com.example.ecommerce.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);
    private final PaymentService paymentService;

    public PaymentConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-events}", groupId = "payment-group")
    public void handleOrderEvent(OrderEvent orderEvent) {
        log.info("Payment Service received OrderEvent for orderId: {}", orderEvent.orderId());
        if ("ORDER_CREATED".equals(orderEvent.status())) {
            paymentService.processPayment(orderEvent);
        }
    }
}