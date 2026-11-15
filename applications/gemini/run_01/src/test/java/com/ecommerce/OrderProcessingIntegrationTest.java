package com.ecommerce;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.NotificationLog;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.NotificationService;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
        })
@EmbeddedKafka(partitions = 1, topics = {"order-events", "payment-events", "inventory-events"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderProcessingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        inventoryService.setInventoryStock("PROD-100", 50);
        inventoryService.setInventoryStock("PROD-LOW", 2);
    }

    @Test
    void shouldProcessOrderSuccessfullyEndToEnd() {
        // FR1a & FR2: Initiate order via REST API -> persists order & publishes order-created event
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-001",
                "PROD-100",
                5,
                new BigDecimal("250.00")
        );

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        var orderId = response.getBody().orderId();

        // Wait for asynchronous Kafka pipeline: Order -> Payment (FR3) -> Inventory (FR4) -> Order Completed & Notifications (FR5)
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<Order> updatedOrder = orderService.getOrder(orderId);
            assertThat(updatedOrder).isPresent();
            assertThat(updatedOrder.get().getStatus()).isEqualTo(OrderStatus.COMPLETED);

            InventoryItem item = inventoryRepository.findById("PROD-100").orElseThrow();
            assertThat(item.getAvailableQuantity()).isEqualTo(45);

            List<NotificationLog> notifications = notificationService.getNotificationsForOrder(orderId);
            assertThat(notifications).extracting(NotificationLog::getStatus)
                    .contains("ORDER_CREATED", "PAYMENT_SUCCESS", "ORDER_COMPLETED");
        });
    }

    @Test
    void shouldHandlePaymentFailureAppropriately() {
        // FR6: Trigger payment failure by exceeding payment limit (> 10000.00)
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-002",
                "PROD-100",
                1,
                new BigDecimal("25000.00")
        );

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var orderId = response.getBody().orderId();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<Order> updatedOrder = orderService.getOrder(orderId);
            assertThat(updatedOrder).isPresent();
            assertThat(updatedOrder.get().getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
            assertThat(updatedOrder.get().getFailureReason()).contains("Payment declined");

            // Ensure inventory was NOT deducted
            InventoryItem item = inventoryRepository.findById("PROD-100").orElseThrow();
            assertThat(item.getAvailableQuantity()).isEqualTo(50);

            // Ensure failure notification was generated
            List<NotificationLog> notifications = notificationService.getNotificationsForOrder(orderId);
            assertThat(notifications).extracting(NotificationLog::getStatus)
                    .contains("ORDER_CREATED", "PAYMENT_FAILED");
        });
    }

    @Test
    void shouldHandleOutOfStockFailureAndTriggerCompensatingRefund() {
        // FR6: Request more stock than available (available: 2, requested: 10)
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-003",
                "PROD-LOW",
                10,
                new BigDecimal("500.00")
        );

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var orderId = response.getBody().orderId();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<Order> updatedOrder = orderService.getOrder(orderId);
            assertThat(updatedOrder).isPresent();
            assertThat(updatedOrder.get().getStatus()).isEqualTo(OrderStatus.FAILED_OUT_OF_STOCK);
            assertThat(updatedOrder.get().getFailureReason()).contains("Insufficient stock");

            // Verify stock remained untouched
            InventoryItem item = inventoryRepository.findById("PROD-LOW").orElseThrow();
            assertThat(item.getAvailableQuantity()).isEqualTo(2);

            // Verify notifications include out-of-stock failure and payment refund
            List<NotificationLog> notifications = notificationService.getNotificationsForOrder(orderId);
            assertThat(notifications).extracting(NotificationLog::getStatus)
                    .contains("ORDER_CREATED", "PAYMENT_SUCCESS", "ORDER_FAILED_OUT_OF_STOCK", "PAYMENT_REFUNDED");
        });
    }
}
