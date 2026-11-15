package com.ecommerce.payment.service;

import com.ecommerce.payment.domain.PaymentRecord;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.shared.event.EventTypes;
import com.ecommerce.shared.event.InventoryEvent;
import com.ecommerce.shared.event.OrderCreatedEvent;
import com.ecommerce.shared.event.PaymentEvent;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentProcessor paymentProcessor;
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentProcessor paymentProcessor,
            PaymentRepository paymentRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.paymentProcessor = paymentProcessor;
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void handleOrderCreated(OrderCreatedEvent orderEvent) {
        PaymentEvent paymentEvent = paymentProcessor.process(orderEvent);
        store(paymentEvent);
        publish(paymentEvent);
        log.info("Processed payment for order {} result={}", orderEvent.orderId(), paymentEvent.eventType());
    }

    public void handleInventoryFailed(InventoryEvent inventoryEvent) {
        PaymentRecord existing = paymentRepository.findByOrderId(inventoryEvent.orderId()).orElse(null);
        if (existing == null || !EventTypes.PAYMENT_COMPLETED.equals(statusToEvent(existing.getStatus()))) {
            log.info("No completed payment to refund for order {}", inventoryEvent.orderId());
            return;
        }

        PaymentEvent refund = PaymentEvent.refunded(
                inventoryEvent.orderId(),
                inventoryEvent.customerId(),
                existing.getAmount(),
                existing.getTransactionId(),
                "Refund due to inventory failure: " + inventoryEvent.reason()
        );
        store(refund);
        publish(refund);
        log.info("Refunded payment for order {} after inventory failure", inventoryEvent.orderId());
    }

    public List<PaymentRecord> findAll() {
        return paymentRepository.findAll();
    }

    private String statusToEvent(String status) {
        return status;
    }

    private void store(PaymentEvent event) {
        paymentRepository.save(new PaymentRecord(
                event.orderId(),
                event.customerId(),
                event.amount(),
                event.eventType(),
                event.transactionId(),
                event.reason(),
                event.timestamp()
        ));
    }

    private void publish(PaymentEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, event.orderId(), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to publish payment event", e);
        }
    }
}
