package com.example.ecommerce.service;

import com.example.ecommerce.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, OrderEvent>> publish(String topic, OrderEvent event) {
        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(topic, event.orderId().toString(), event);
        future.whenComplete((result, error) -> {
            if (error == null) {
                log.info("Published event {} for order {} to {}", event.type(), event.orderId(), topic);
            } else {
                log.error("Failed to publish event {} for order {} to {}", event.type(), event.orderId(), topic, error);
            }
        });
        return future;
    }
}
