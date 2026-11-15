package com.example.ecommerce.config;

import com.example.ecommerce.model.OrderEvent;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events").partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic paymentEventsTopic() {
        return TopicBuilder.name("payment-events").partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic inventoryEventsTopic() {
        return TopicBuilder.name("inventory-events").partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic orderEventsDlt() {
        return TopicBuilder.name("order-events.DLT").partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic paymentEventsDlt() {
        return TopicBuilder.name("payment-events.DLT").partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic inventoryEventsDlt() {
        return TopicBuilder.name("inventory-events.DLT").partitions(3).replicas(1).build();
    }

    @Bean
    CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}
