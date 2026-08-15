package com.example.notification.service;

import com.example.common.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceTest {

    @Test
    void notifyOrderCreated_shouldNotThrow() {
        NotificationService service = new NotificationService();
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1, 10.0, Instant.now());
        assertDoesNotThrow(() -> service.notifyOrderCreated(event));
    }
}
