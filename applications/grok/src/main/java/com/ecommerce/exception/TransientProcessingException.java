package com.ecommerce.exception;

public class TransientProcessingException extends RuntimeException {

    public TransientProcessingException(String message) {
        super(message);
    }
}
