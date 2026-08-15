package com.ecommerce.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.ecommerce.common.JsonEventMapper;
import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.InventoryEvent;
import com.ecommerce.exception.EventPublishingException;

@Component
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonEventMapper jsonEventMapper;

    public InventoryEventProducer(KafkaTemplate<String, String> kafkaTemplate, JsonEventMapper jsonEventMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonEventMapper = jsonEventMapper;
    }

    public void publish(InventoryEvent event) {
        try {
            String payload = jsonEventMapper.write(event);
            kafkaTemplate.send(KafkaTopics.INVENTORY_EVENTS, event.getOrderId(), payload).get();
            log.info("Published {} for order {}", event.getEventType(), event.getOrderId());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new EventPublishingException("Interrupted while publishing inventory event", ex);
        } catch (Exception ex) {
            throw new EventPublishingException("Failed to publish inventory event for " + event.getOrderId(), ex);
        }
    }
}
