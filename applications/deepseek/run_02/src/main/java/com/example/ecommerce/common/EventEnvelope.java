package com.example.ecommerce.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventEnvelope<T> {
    private String eventType;
    private T payload;
    private long timestamp;

    public static <T> EventEnvelope<T> of(String eventType, T payload) {
        return new EventEnvelope<>(eventType, payload, System.currentTimeMillis());
    }
}