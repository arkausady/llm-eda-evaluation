package com.example.orderprocessing.service;

import com.example.orderprocessing.exception.EventPublicationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event)
                    .get(10, TimeUnit.SECONDS);

        } catch (Exception exception) {
            throw new EventPublicationException(
                    "Unable to publish event to topic " + topic,
                    exception
            );
        }
    }
}