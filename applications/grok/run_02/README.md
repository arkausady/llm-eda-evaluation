# E-Commerce Order Processing

Event-driven order processing with Java 25, Spring Boot, Maven, and Apache Kafka.

## Services

- order-service (8081): create/store orders and publish `order-events`
- payment-service (8082): process payment from `order-events` and publish `payment-events`
- inventory-service (8083): reserve stock from successful `payment-events` and publish `inventory-events`
- notification-service (8084): generate status notifications from all business events

## Prerequisites

- JDK 25
- Maven 3.9+
- Docker (for Kafka)

## Run Kafka

```bash
docker compose up -d
```

## Build

```bash
mvn clean verify
```

## Run services

```bash
mvn -pl order-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl notification-service spring-boot:run
```

## Create an order

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-1","items":[{"productId":"SKU-001","quantity":2,"unitPrice":19.99}]}'
```

Payment failure: use customerId containing `FAIL` or a total greater than 10000.
Inventory failure: use productId `SKU-003`.
