package com.ecommerce.orderprocessing.payment;

import com.ecommerce.orderprocessing.event.OrderCreatedEvent;
import com.ecommerce.orderprocessing.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final Set<String> DECLINED_METHODS = Set.of("FAIL", "DECLINED", "REJECT");

    public PaymentEvent process(OrderCreatedEvent orderEvent) {
        boolean approved = isApproved(orderEvent);
        String message = approved
                ? "Payment captured successfully"
                : "Payment declined for method " + orderEvent.paymentMethod();
        PaymentEvent event = new PaymentEvent(
                UUID.randomUUID().toString(),
                approved ? PaymentEvent.PROCESSED : PaymentEvent.FAILED,
                Instant.now(),
                orderEvent.orderId(),
                orderEvent.customerId(),
                orderEvent.customerEmail(),
                orderEvent.items(),
                orderEvent.totalAmount(),
                orderEvent.paymentMethod(),
                approved,
                message
        );
        log.info("Payment for order {} success={}", orderEvent.orderId(), approved);
        return event;
    }

    boolean isApproved(OrderCreatedEvent orderEvent) {
        if (orderEvent.totalAmount() == null || orderEvent.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        String method = orderEvent.paymentMethod() == null ? "" : orderEvent.paymentMethod().trim().toUpperCase(Locale.ROOT);
        return !DECLINED_METHODS.contains(method);
    }
}
