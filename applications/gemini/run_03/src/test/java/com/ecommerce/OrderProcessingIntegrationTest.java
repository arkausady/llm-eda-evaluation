package com.ecommerce;

import com.ecommerce.config.KafkaConfig;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.NotificationLog;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.service.NotificationService;
import com.ecommerce.service.OrderService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
        partitions = 1,
        topics = {
                KafkaConfig.ORDER_EVENTS_TOPIC,
                KafkaConfig.PAYMENT_EVENTS_TOPIC,
                KafkaConfig.INVENTORY_EVENTS_TOPIC
        }
)
class OrderProcessingIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setupInventory() {
        inventoryRepository.save(new InventoryItem("PROD-101", 50));
        inventoryRepository.save(new InventoryItem("PROD-LOW", 2));
    }

    @Test
    @DisplayName("FR1-FR5: Complete end-to-end successful order processing flow")
    void testSuccessfulOrderFlow() {
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-001",
                "PROD-101",
                5,
                new BigDecimal("250.00")
        );

        OrderResponse createdOrder = orderService.createOrder(request);
        assertThat(createdOrder.orderId()).isNotNull();
        assertThat(createdOrder.status()).isEqualTo(OrderStatus.CREATED);

        // Wait for async Kafka event pipeline (Order -> Payment -> Inventory -> Order Completion & Notifications)
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    OrderResponse updatedOrder = orderService.getOrder(createdOrder.orderId());
                    assertThat(updatedOrder.status()).isEqualTo(OrderStatus.COMPLETED);

                    InventoryItem item = inventoryRepository.findById("PROD-101").orElseThrow();
                    assertThat(item.getAvailableQuantity()).isEqualTo(45);

                    List<NotificationLog> notifications = notificationService.getNotificationsForOrder(createdOrder.orderId());
                    assertThat(notifications).extracting(NotificationLog::getStatusType)
                            .contains("ORDER_CREATED", "PAYMENT_SUCCESS", "ORDER_COMPLETED");
                });
    }

    @Test
    @DisplayName("FR6: Handle payment failure when total amount exceeds maximum allowed limit")
    void testPaymentFailureFlow() {
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-002",
                "PROD-101",
                1,
                new BigDecimal("25000.00") // Exceeds 10,000 limit
        );

        OrderResponse createdOrder = orderService.createOrder(request);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    OrderResponse updatedOrder = orderService.getOrder(createdOrder.orderId());
                    assertThat(updatedOrder.status()).isEqualTo(OrderStatus.FAILED);
                    assertThat(updatedOrder.failureReason()).contains("exceeds maximum limit");

                    // Verify inventory was not deducted
                    InventoryItem item = inventoryRepository.findById("PROD-101").orElseThrow();
                    assertThat(item.getAvailableQuantity()).isEqualTo(50);

                    List<NotificationLog> notifications = notificationService.getNotificationsForOrder(createdOrder.orderId());
                    assertThat(notifications).extracting(NotificationLog::getStatusType)
                            .contains("ORDER_CREATED", "PAYMENT_FAILED");
                });
    }

    @Test
    @DisplayName("FR6: Handle inventory out-of-stock failure and trigger compensating payment refund")
    void testInventoryFailureAndCompensatingRefundFlow() {
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-003",
                "PROD-LOW",
                10, // Only 2 available in stock
                new BigDecimal("500.00")
        );

        OrderResponse createdOrder = orderService.createOrder(request);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    OrderResponse updatedOrder = orderService.getOrder(createdOrder.orderId());
                    assertThat(updatedOrder.status()).isEqualTo(OrderStatus.FAILED);
                    assertThat(updatedOrder.failureReason()).contains("Insufficient stock");

                    // Verify inventory remained untouched
                    InventoryItem item = inventoryRepository.findById("PROD-LOW").orElseThrow();
                    assertThat(item.getAvailableQuantity()).isEqualTo(2);

                    // Verify notifications include payment refund compensation
                    List<NotificationLog> notifications = notificationService.getNotificationsForOrder(createdOrder.orderId());
                    assertThat(notifications).extracting(NotificationLog::getStatusType)
                            .contains("ORDER_CREATED", "PAYMENT_SUCCESS", "INVENTORY_FAILED", "PAYMENT_REFUNDED");
                });
    }
}
