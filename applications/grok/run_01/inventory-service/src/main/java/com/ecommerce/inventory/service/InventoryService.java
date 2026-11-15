package com.ecommerce.inventory.service;

import com.ecommerce.inventory.domain.StockItem;
import com.ecommerce.inventory.repository.StockRepository;
import com.ecommerce.shared.event.InventoryEvent;
import com.ecommerce.shared.event.PaymentEvent;
import com.ecommerce.shared.kafka.KafkaTopics;
import com.ecommerce.shared.model.OrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final StockRepository stockRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InventoryService(
            StockRepository stockRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.stockRepository = stockRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public InventoryEvent reserveForPayment(PaymentEvent paymentEvent) {
        if (paymentEvent.customerId() != null && paymentEvent.customerId().toLowerCase().contains("crash-inv")) {
            throw new IllegalStateException("Simulated technical inventory failure");
        }

        InventoryEvent result;
        synchronized (this) {
            List<String> shortages = new ArrayList<>();
            for (OrderItem item : paymentEvent.items()) {
                int available = stockRepository.findByProductId(item.productId())
                        .map(StockItem::getQuantity)
                        .orElse(0);
                if (available < item.quantity()) {
                    shortages.add(item.productId() + " (need " + item.quantity() + ", have " + available + ")");
                }
            }

            if (!shortages.isEmpty()) {
                result = InventoryEvent.failed(
                        paymentEvent.orderId(),
                        paymentEvent.customerId(),
                        paymentEvent.items(),
                        "Insufficient stock: " + String.join(", ", shortages)
                );
            } else {
                for (OrderItem item : paymentEvent.items()) {
                    StockItem stockItem = stockRepository.findByProductId(item.productId()).orElseThrow();
                    stockItem.setQuantity(stockItem.getQuantity() - item.quantity());
                    stockRepository.save(stockItem);
                }
                result = InventoryEvent.reserved(
                        paymentEvent.orderId(),
                        paymentEvent.customerId(),
                        paymentEvent.items()
                );
            }
        }

        publish(result);
        log.info("Inventory result for order {}: {}", paymentEvent.orderId(), result.eventType());
        return result;
    }

    public List<StockItem> currentStock() {
        return stockRepository.findAll();
    }

    private void publish(InventoryEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.INVENTORY_EVENTS, event.orderId(), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to publish inventory event", e);
        }
    }
}
