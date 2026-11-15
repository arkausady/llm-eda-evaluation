# Order Processing Application

Event-driven e-commerce order processing built with Java 25, Spring Boot, Maven, and Apache Kafka.

## Services

- Order Service: create/store orders and publish `ORDER_CREATED` to `order-events`
- Payment Service: consume order events, process payment, publish to `payment-events`
- Inventory Service: consume successful payments, update stock, publish to `inventory-events`
- Notification Service: consume all business events and generate status notifications

## Prerequisites

- Java 25
- Maven 3.9+
- Docker (for Kafka)

## Run

```bash
docker compose up -d
mvn spring-boot:run
```

## Sample requests

Create order (happy path):

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"CUST-1","customerEmail":"alice@example.com","paymentMethod":"CREDIT_CARD","items":[{"productId":"PROD-001","productName":"Laptop","quantity":1,"unitPrice":999.99}]}'
```

Force payment failure with `paymentMethod` `FAIL`.

Force inventory failure by ordering quantity greater than available stock.

## Tests

```bash
mvn test
```
