package com.ecommerce.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.ecommerce.common.JsonEventMapper;
import com.ecommerce.config.KafkaTopics;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.OrderEventType;
import com.ecommerce.exception.EventPublishingException;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.OrderItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OrderEventProducer producer;
    private JsonEventMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper = new JsonEventMapper(objectMapper);
        producer = new OrderEventProducer(kafkaTemplate, mapper);
    }

    @Test
    void publishSendsSerializedEventToOrderTopic() {
        CustomerOrder order = CustomerOrder.create(
                "cust-1",
                "buyer@example.com",
                List.of(new OrderItem("SKU-001", "Headphones", 1, new BigDecimal("10.00"))),
                "CARD"
        );
        OrderEvent event = OrderEvent.of(OrderEventType.ORDER_CREATED, order, null);
        when(kafkaTemplate.send(eq(KafkaTopics.ORDER_EVENTS), eq(order.getOrderId()), any(String.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        producer.publish(event);

        verify(kafkaTemplate).send(eq(KafkaTopics.ORDER_EVENTS), eq(order.getOrderId()), any(String.class));
    }

    @Test
    void publishWrapsFailures() {
        CustomerOrder order = CustomerOrder.create(
                "cust-1",
                "buyer@example.com",
                List.of(new OrderItem("SKU-001", "Headphones", 1, new BigDecimal("10.00"))),
                "CARD"
        );
        OrderEvent event = OrderEvent.of(OrderEventType.ORDER_CREATED, order, null);
        when(kafkaTemplate.send(eq(KafkaTopics.ORDER_EVENTS), eq(order.getOrderId()), any(String.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

        EventPublishingException ex = assertThrows(EventPublishingException.class, () -> producer.publish(event));
        assertEquals(true, ex.getMessage().contains(order.getOrderId()));
    }
}
