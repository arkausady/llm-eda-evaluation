package com.ecommerce.orderprocessing.order;

import com.ecommerce.orderprocessing.config.Topics;
import com.ecommerce.orderprocessing.event.EventParser;
import com.ecommerce.orderprocessing.event.OrderCreatedEvent;
import com.ecommerce.orderprocessing.exception.OrderProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventParser eventParser;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate, EventParser eventParser) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventParser = eventParser;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            String payload = eventParser.write(event);
            kafkaTemplate.send(Topics.ORDER_EVENTS, event.orderId(), payload).get();
            log.info("Published {} for order {}", event.eventType(), event.orderId());
        } catch (Exception e) {
            throw new OrderProcessingException("Failed to publish order-created event for order " + event.orderId(), e);
        }
    }
}
