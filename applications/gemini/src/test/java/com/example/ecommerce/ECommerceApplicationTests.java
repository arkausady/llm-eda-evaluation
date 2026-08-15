package com.example.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order-events", "payment-events", "inventory-events"})
@DirtiesContext
class ECommerceApplicationTests {

    @Test
    void contextLoads() {
    }
}