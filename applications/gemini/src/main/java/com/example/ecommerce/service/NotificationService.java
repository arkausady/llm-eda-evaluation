package com.example.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendOrderNotification(String orderId, String status, String details) {
        log.info("FR5: Notification generated for Order {}. Status: {}. Details: {}", orderId, status, details);
    }
}