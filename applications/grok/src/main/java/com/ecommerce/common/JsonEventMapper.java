package com.ecommerce.common;

import org.springframework.stereotype.Component;

import com.ecommerce.exception.EventPublishingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonEventMapper {

    private final ObjectMapper objectMapper;

    public JsonEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String write(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new EventPublishingException("Failed to serialize event " + event.getClass().getSimpleName(), ex);
        }
    }

    public <T> T read(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize payload to " + type.getSimpleName(), ex);
        }
    }
}
