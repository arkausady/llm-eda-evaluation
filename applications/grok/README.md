# Event-Driven Order Processing

Spring Boot / Java 25 e-commerce order workflow using Apache Kafka.

## Services

- Order service: REST create/store order, publish `ORDER_CREATED` to `order-events`, update status from payment and inventory events
- Payment service: consume `order-events`, process payment, publish to `payment-events`
- Inventory service: consume successful `payment-events`, reserve stock, publish to `inventory-events`
- Notification service: consume all three topics and store order status notifications

## Failure handling

- Business payment decline: set `paymentMethod` to `FAIL_PAYMENT` (or customerId `fail-payment`)
- Insufficient stock: order SKU-003 with quantity greater than 2
- Transient Kafka-consumer retries: set `paymentMethod` to `TRANSIENT_ERROR` (retried then sent to DLT)
- Compensation: inventory failure triggers `PAYMENT_REFUNDED` and order cancellation
- Technical failures: retries with exponential backoff, then `*.DLT` topics and `PROCESSING_ERROR` status

## Prerequisites

- JDK 25
- Maven 3.9+
- Docker (for Kafka)

## Run

```bash
docker compose up -d
mvn test
mvn spring-boot:run
```

Create an order:

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "cust-1",
    "customerEmail": "buyer@example.com",
    "paymentMethod": "CARD",
    "items": [{"sku": "SKU-001", "quantity": 1}]
  }'
```

Inspect status, inventory, and notifications:

```bash
curl -s http://localhost:8080/api/orders
curl -s http://localhost:8080/api/inventory
curl -s http://localhost:8080/api/notifications
```
