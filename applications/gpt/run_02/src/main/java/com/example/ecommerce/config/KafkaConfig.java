package com.example.ecommerce.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    KafkaAdmin.NewTopics ecommerceTopics() {
        return new KafkaAdmin.NewTopics(
                org.springframework.kafka.config.TopicBuilder.name(KafkaTopics.ORDER_EVENTS).partitions(3).replicas(1).build(),
                org.springframework.kafka.config.TopicBuilder.name(KafkaTopics.PAYMENT_EVENTS).partitions(3).replicas(1).build(),
                org.springframework.kafka.config.TopicBuilder.name(KafkaTopics.INVENTORY_EVENTS).partitions(3).replicas(1).build());
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(500L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(4_000L);
        DefaultErrorHandler handler = new DefaultErrorHandler((record, exception) ->
                log.error("Kafka record exhausted retries: topic={}, partition={}, offset={}",
                        record.topic(), record.partition(), record.offset(), exception), backOff);
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
