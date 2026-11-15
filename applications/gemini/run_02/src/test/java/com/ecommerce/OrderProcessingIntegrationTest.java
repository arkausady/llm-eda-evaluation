package com.ecommerce;

import com.ecommerce.config.KafkaTopics;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.model.InventoryItem;
import com.ecommerce.model.NotificationLog;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.InventoryRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
        })
@EmbeddedKafka(partitions = 1, topics = {
        KafkaTopics.ORDER_EVENTS,
        KafkaTopics.PAYMENT_EVENTS,
        KafkaTopics.INVENTORY_EVENTS,
        KafkaTopics.ORDER_EVENTS_DLT
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderProcessingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.save(new InventoryItem("PROD-100", 50));
        inventoryRepository.save(new InventoryItem("PROD-200", 2));
    }

    @Test
    void shouldProcessOrderSuccessfullyEndToEnd() {
        // Given: A valid order request
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-001",
                "PROD-100",
                5,
                new BigDecimal("250.00")
        );

        // When: Order is submitted via REST API (FR1a, FR2)
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();

        UUID orderId = createResponse.getBody().orderId();

        // Then: Asynchronous events process payment, update inventory, and complete order (FR3, FR4, FR5)
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    ResponseEntity<OrderResponse> statusResponse = restTemplate.getForEntity("/api/orders/" + orderId, OrderResponse.class);
                    assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(statusResponse.getBody()).isNotNull();
                    assertThat(statusResponse.getBody().status()).isEqualTo(OrderStatus.COMPLETED);
                });

        // Verify inventory was decremented (FR4)
        InventoryItem updatedItem = inventoryRepository.findById("PROD-100").orElseThrow();
        assertThat(updatedItem.getAvailableQuantity()).isEqualTo(45);

        // Verify notifications were generated (FR5)
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    ResponseEntity<List<NotificationLog>> notifResponse = restTemplate.exchange(
                            "/api/orders/" + orderId + "/notifications",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {}
                    );
                    List<NotificationLog> logs = notifResponse.getBody();
                    assertThat(logs).isNotNull();
                    assertThat(logs).extracting(NotificationLog::getStatus)
                            .contains("ORDER_CREATED", "PAYMENT_SUCCESS", "ORDER_COMPLETED");
                });
    }

    @Test
    void shouldHandlePaymentFailureGracefully() {
        // Given: An order exceeding the maximum allowed payment limit (FR6)
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-002",
                "PROD-100",
                1,
                new BigDecimal("25000.00")
        );

        // When: Order is submitted
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID orderId = createResponse.getBody().orderId();

        // Then: Order transitions to PAYMENT_FAILED and inventory remains untouched
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    ResponseEntity<OrderResponse> statusResponse = restTemplate.getForEntity("/api/orders/" + orderId, OrderResponse.class);
                    assertThat(statusResponse.getBody()).isNotNull();
                    assertThat(statusResponse.getBody().status()).isEqualTo(OrderStatus.PAYMENT_FAILED);
                    assertThat(statusResponse.getBody().failureReason()).contains("exceeds credit limit");
                });

        InventoryItem item = inventoryRepository.findById("PROD-100").orElseThrow();
        assertThat(item.getAvailableQuantity()).isEqualTo(50);

        // Verify failure notification was generated (FR5, FR6)
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    ResponseEntity<List<NotificationLog>> notifResponse = restTemplate.exchange(
                            "/api/orders/" + orderId + "/notifications",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {}
                    );
                    assertThat(notifResponse.getBody()).extracting(NotificationLog::getStatus)
                            .contains("ORDER_CREATED", "PAYMENT_FAILED");
                });
    }

    @Test
    void shouldHandleInventoryInsufficientStockFailure() {
        // Given: An order requesting more items than available in stock (FR6)
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-003",
                "PROD-200",
                10,
                new BigDecimal("500.00")
        );

        // When: Order is submitted
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID orderId = createResponse.getBody().orderId();

        // Then: Payment succeeds, but inventory fails, marking order as INVENTORY_FAILED
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    ResponseEntity<OrderResponse> statusResponse = restTemplate.getForEntity("/api/orders/" + orderId, OrderResponse.class);
                    assertThat(statusResponse.getBody()).isNotNull();
                    assertThat(statusResponse.getBody().status()).isEqualTo(OrderStatus.INVENTORY_FAILED);
                    assertThat(statusResponse.getBody().failureReason()).contains("Insufficient stock");
                });

        // Inventory stock should remain unchanged at 2
        InventoryItem item = inventoryRepository.findById("PROD-200").orElseThrow();
        assertThat(item.getAvailableQuantity()).isEqualTo(2);

        // Verify failure notification was generated (FR5, FR6)
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    ResponseEntity<List<NotificationLog>> notifResponse = restTemplate.exchange(
                            "/api/orders/" + orderId + "/notifications",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {}
                    );
                    assertThat(notifResponse.getBody()).extracting(NotificationLog::getStatus)
                            .contains("ORDER_CREATED", "PAYMENT_SUCCESS", "ORDER_FAILED_INVENTORY");
                });
    }
}