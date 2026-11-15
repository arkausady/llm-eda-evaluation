package com.example.ecommerce.service;

import com.example.ecommerce.config.KafkaTopics;
import com.example.ecommerce.event.OrderCreatedEvent;
import com.example.ecommerce.event.PaymentEvent;
import com.example.ecommerce.event.PaymentStatus;
import com.example.ecommerce.messaging.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal MAX_PAYMENT = new BigDecimal("10000.00");
    private final EventPublisher eventPublisher;
    private final Set<String> processedOrders = ConcurrentHashMap.newKeySet();

    public PaymentService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "payment-service")
    public void processPayment(OrderCreatedEvent event) {
        if (processedOrders.contains(event.orderId())) {
            log.info("Payment already processed for order {}", event.orderId());
            return;
        }

        PaymentStatus status = PaymentStatus.COMPLETED;
        String reason = null;
        if ("DECLINE".equalsIgnoreCase(event.paymentReference())) {
            status = PaymentStatus.FAILED;
            reason = "Payment was declined";
        } else if (event.totalAmount().compareTo(MAX_PAYMENT) > 0) {
            status = PaymentStatus.FAILED;
            reason = "Payment exceeds the permitted limit";
        }

        PaymentEvent paymentEvent = new PaymentEvent(UUID.randomUUID().toString(), event.orderId(),
                event.productId(), event.quantity(), status, reason, Instant.now());
        eventPublisher.publish(KafkaTopics.PAYMENT_EVENTS, event.orderId(), paymentEvent).join();
        processedOrders.add(event.orderId());
        log.info("Payment result for order {}: {}", event.orderId(), status);
    }
}
