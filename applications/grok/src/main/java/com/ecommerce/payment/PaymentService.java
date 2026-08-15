package com.ecommerce.payment;

import java.math.BigDecimal;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentEventType;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.PaymentRecord;
import com.ecommerce.model.PaymentStatus;

@Service
public class PaymentService {

    public static final String FAIL_METHOD = "FAIL_PAYMENT";
    public static final String TRANSIENT_METHOD = "TRANSIENT_ERROR";

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    public PaymentRecord processOrderCreated(OrderEvent event) {
        CustomerOrder order = event.getOrder();
        PaymentDecision decision = decide(order);
        PaymentRecord payment = PaymentRecord.of(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                decision.status(),
                decision.reason()
        );
        paymentRepository.save(payment);
        PaymentEvent paymentEvent = PaymentEvent.from(decision.eventType(), payment, order, decision.reason());
        paymentEventProducer.publish(paymentEvent);
        log.info("Processed payment {} for order {} with status {}",
                payment.getPaymentId(), order.getOrderId(), payment.getStatus());
        return payment;
    }

    public PaymentRecord refund(CustomerOrder order, String reason) {
        PaymentRecord refund = PaymentRecord.of(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                PaymentStatus.REFUNDED,
                reason
        );
        paymentRepository.save(refund);
        paymentEventProducer.publish(PaymentEvent.from(PaymentEventType.PAYMENT_REFUNDED, refund, order, reason));
        log.info("Refunded payment for order {} because {}", order.getOrderId(), reason);
        return refund;
    }

    PaymentDecision decide(CustomerOrder order) {
        String method = order.getPaymentMethod() == null ? "CARD" : order.getPaymentMethod().toUpperCase(Locale.ROOT);
        if (TRANSIENT_METHOD.equals(method)) {
            throw new com.ecommerce.exception.TransientProcessingException(
                    "Simulated transient payment gateway failure for order " + order.getOrderId());
        }
        if (FAIL_METHOD.equals(method) || "fail-payment".equalsIgnoreCase(order.getCustomerId())) {
            return new PaymentDecision(PaymentStatus.FAILED, PaymentEventType.PAYMENT_FAILED, "Payment declined");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentDecision(PaymentStatus.FAILED, PaymentEventType.PAYMENT_FAILED, "Invalid payment amount");
        }
        return new PaymentDecision(PaymentStatus.COMPLETED, PaymentEventType.PAYMENT_COMPLETED, null);
    }

    record PaymentDecision(PaymentStatus status, PaymentEventType eventType, String reason) {
    }
}
