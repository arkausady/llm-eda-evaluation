package com.example.ecommerce.messaging;

import java.util.concurrent.CompletableFuture;

public interface EventPublisher {
    CompletableFuture<Void> publish(String topic, String key, Object event);
}
