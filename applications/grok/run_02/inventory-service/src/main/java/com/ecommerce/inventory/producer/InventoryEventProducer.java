package com.ecommerce.inventory.producer;

import com.ecommerce.common.event.InventoryEvent;
import com.ecommerce.common.exception.ProcessingException;
import com.ecommerce.common.kafka.KafkaTopics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InventoryEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(InventoryEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.INVENTORY_EVENTS, event.orderId(), payload);
            log.info("Published inventory event orderId={} status={}", event.orderId(), event.status());
        } catch (JsonProcessingException e) {
            throw new ProcessingException("Failed to serialize inventory event", e);
        }
    }
}
