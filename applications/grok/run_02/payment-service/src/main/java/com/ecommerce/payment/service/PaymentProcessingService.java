package com.ecommerce.payment.service;

import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.event.PaymentEvent;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.payment.producer.PaymentEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentProcessingService {

    static final BigDecimal PAYMENT_LIMIT = new BigDecimal("10000");
    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final PaymentEventProducer paymentEventProducer;

    public PaymentProcessingService(PaymentEventProducer paymentEventProducer) {
        this.paymentEventProducer = paymentEventProducer;
    }

    public PaymentEvent process(OrderCreatedEvent orderEvent) {
        if (orderEvent == null || orderEvent.orderId() == null) {
            throw new ProcessingException("Invalid order-created event");
        }
        PaymentEvent paymentEvent = decide(orderEvent);
        paymentEventProducer.publish(paymentEvent);
        log.info("Processed payment for order {} with status {}", orderEvent.orderId(), paymentEvent.status());
        return paymentEvent;
    }

    public PaymentEvent decide(OrderCreatedEvent orderEvent) {
        if (orderEvent.customerId() != null && orderEvent.customerId().toUpperCase().contains("FAIL")) {
            return PaymentEvent.failed(orderEvent, "Payment declined for customer");
        }
        if (orderEvent.totalAmount() != null && orderEvent.totalAmount().compareTo(PAYMENT_LIMIT) > 0) {
            return PaymentEvent.failed(orderEvent, "Amount exceeds payment limit of " + PAYMENT_LIMIT);
        }
        if (orderEvent.totalAmount() == null || orderEvent.totalAmount().signum() <= 0) {
            return PaymentEvent.failed(orderEvent, "Invalid payment amount");
        }
        return PaymentEvent.success(orderEvent);
    }

    public void compensate(String orderId, String reason) {
        log.warn("Compensating/refunding payment for order {} due to {}", orderId, reason);
    }
}
