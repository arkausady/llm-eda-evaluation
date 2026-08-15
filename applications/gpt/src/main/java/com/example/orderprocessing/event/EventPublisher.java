package com.example.orderprocessing.event;

import com.example.orderprocessing.exception.EventPublicationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EventPublisher {
    private final KafkaTemplate<String, BusinessEvent> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, BusinessEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, BusinessEvent event) {
        try {
            kafkaTemplate.send(topic, event.orderId().toString(), event).get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            Thread.currentThread().interrupt();
            throw new EventPublicationException("Unable to publish event to " + topic, exception);
        }
    }
}
