# Event-Driven E-Commerce Order Processing

Java 25 / Spring Boot / Maven / Apache Kafka application implementing:

- FR1a Create and store a customer order
- FR2 Publish an order-created event
- FR3 Process payment from the received event
- FR4 Update inventory after order processing
- FR5 Generate order status notification
- FR6 Handle processing failure appropriately

## Services

| Service | Port | Role |
| --- | --- | --- |
| order-service | 8081 | REST order API, publishes `order-events`, updates status from payment/inventory |
| payment-service | 8082 | Consumes `order-events`, publishes `payment-events`, refunds on inventory failure |
| inventory-service | 8083 | Consumes successful `payment-events`, publishes `inventory-events` |
| notification-service | 8084 | Consumes all business topics and stores order status notifications |

Kafka topics: `order-events`, `payment-events`, `inventory-events`. Technical failures are retried then sent to `.DLT` topics.

## Prerequisites

- JDK 25
- Maven 3.9+
- Docker (for Kafka)

## Run

```bash
docker compose up -d
mvn clean install
```

Start each service in a separate terminal:

```bash
mvn -pl order-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl notification-service spring-boot:run
```

## Sample requests

Happy path:

```bash
curl -s -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-1","items":[{"productId":"PROD-1","productName":"Laptop","quantity":1,"unitPrice":999.99}]}'
```

Payment declined (customerId contains `fail`):

```bash
curl -s -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"fail-user","items":[{"productId":"PROD-2","productName":"Mouse","quantity":1,"unitPrice":19.99}]}'
```

Inventory failure (insufficient stock) triggers compensation refund:

```bash
curl -s -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-1","items":[{"productId":"PROD-3","productName":"Keyboard","quantity":1000,"unitPrice":29.99}]}'
```

Technical failure (retried, then DLT) when customerId contains `crash`.

Inspect results:

```bash
curl -s http://localhost:8081/api/orders
curl -s http://localhost:8082/api/payments
curl -s http://localhost:8083/api/inventory
curl -s http://localhost:8084/api/notifications
```
