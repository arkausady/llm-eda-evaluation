package com.example.ecommerce.inventory;

import com.example.ecommerce.common.EventEnvelope;
import com.example.ecommerce.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.inventory-events}")
    private String inventoryEventsTopic;

    @KafkaListener(topics = "${kafka.topics.payment-events}", groupId = "inventory-group")
    public void onPaymentEvent(EventEnvelope<PaymentEvent> envelope) {
        PaymentEvent paymentEvent = envelope.getPayload();
        if ("SUCCESS".equals(paymentEvent.getPaymentStatus())) {
            updateInventory(paymentEvent.getOrderId());
        } else {
            // Payment failed, no inventory update
            publishInventoryFailure(paymentEvent.getOrderId());
        }
    }

    public void updateInventory(String orderId) {
        // Simulate inventory update
        InventoryEvent event = new InventoryEvent(orderId, "UPDATED", System.currentTimeMillis());
        EventEnvelope<InventoryEvent> envelope = EventEnvelope.of("InventoryUpdated", event);
        kafkaTemplate.send(inventoryEventsTopic, orderId, envelope);
    }

    private void publishInventoryFailure(String orderId) {
        InventoryEvent event = new InventoryEvent(orderId, "FAILED", System.currentTimeMillis());
        EventEnvelope<InventoryEvent> envelope = EventEnvelope.of("InventoryFailed", event);
        kafkaTemplate.send(inventoryEventsTopic, orderId, envelope);
    }
}