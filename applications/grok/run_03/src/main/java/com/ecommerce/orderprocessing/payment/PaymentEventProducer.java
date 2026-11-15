package com.ecommerce.orderprocessing.payment;

import com.ecommerce.orderprocessing.config.Topics;
import com.ecommerce.orderprocessing.event.EventParser;
import com.ecommerce.orderprocessing.event.PaymentEvent;
import com.ecommerce.orderprocessing.exception.OrderProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventParser eventParser;

    public PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate, EventParser eventParser) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventParser = eventParser;
    }

    public void publish(PaymentEvent event) {
        try {
            kafkaTemplate.send(Topics.PAYMENT_EVENTS, event.orderId(), eventParser.write(event)).get();
            log.info("Published {} for order {}", event.eventType(), event.orderId());
        } catch (Exception e) {
            throw new OrderProcessingException("Failed to publish payment event for order " + event.orderId(), e);
        }
    }
}
