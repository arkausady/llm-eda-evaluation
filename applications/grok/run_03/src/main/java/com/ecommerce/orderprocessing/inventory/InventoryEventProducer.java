package com.ecommerce.orderprocessing.inventory;

import com.ecommerce.orderprocessing.config.Topics;
import com.ecommerce.orderprocessing.event.EventParser;
import com.ecommerce.orderprocessing.event.InventoryEvent;
import com.ecommerce.orderprocessing.exception.OrderProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventParser eventParser;

    public InventoryEventProducer(KafkaTemplate<String, String> kafkaTemplate, EventParser eventParser) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventParser = eventParser;
    }

    public void publish(InventoryEvent event) {
        try {
            kafkaTemplate.send(Topics.INVENTORY_EVENTS, event.orderId(), eventParser.write(event)).get();
            log.info("Published {} for order {}", event.eventType(), event.orderId());
        } catch (Exception e) {
            throw new OrderProcessingException("Failed to publish inventory event for order " + event.orderId(), e);
        }
    }
}
