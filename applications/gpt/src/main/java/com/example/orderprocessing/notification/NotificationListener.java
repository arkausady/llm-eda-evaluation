package com.example.orderprocessing.notification;

import com.example.orderprocessing.event.BusinessEvent;
import com.example.orderprocessing.event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationRepository repository;

    public NotificationListener(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @KafkaListener(topics = "order-events", groupId = "order-notifications")
    public void receive(BusinessEvent event) {
        if (event.type() != EventType.ORDER_COMPLETED && event.type() != EventType.ORDER_FAILED) {
            return;
        }
        String status = event.type().name();
        if (!repository.existsByOrderIdAndStatus(event.orderId(), status)) {
            repository.save(new NotificationEntity(event.orderId(), status, event.message()));
            log.info("Order notification generated: orderId={}, status={}, message={}",
                    event.orderId(), status, event.message());
        }
    }
}
