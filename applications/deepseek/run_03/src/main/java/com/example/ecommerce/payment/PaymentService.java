package com.example.ecommerce.payment;

import com.example.ecommerce.model.OrderCreatedEvent;
import com.example.ecommerce.model.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal LIMIT = new BigDecimal("1000.00");

    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(PaymentEventPublisher paymentEventPublisher) {
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public void processPayment(OrderCreatedEvent orderEvent) {
        try {
            boolean success = orderEvent.getAmount().compareTo(LIMIT) <= 0;
            String status = success ? "SUCCESS" : "FAILED";
            String message = success ? "Payment processed successfully" : "Payment failed: amount exceeds limit";
            PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(
                    orderEvent.getOrderId(),
                    status,
                    message,
                    orderEvent.getAmount()
            );
            paymentEventPublisher.publishPaymentProcessed(paymentEvent);
            log.info("Payment processed for order {}: {}", orderEvent.getOrderId(), status);
        } catch (Exception e) {
            log.error("Error processing payment for order {}", orderEvent.getOrderId(), e);
            // Publish failure event with error message
            PaymentProcessedEvent failureEvent = new PaymentProcessedEvent(
                    orderEvent.getOrderId(),
                    "FAILED",
                    "Payment processing error: " + e.getMessage(),
                    orderEvent.getAmount()
            );
            paymentEventPublisher.publishPaymentProcessed(failureEvent);
        }
    }
}
