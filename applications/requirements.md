# Functional Requirements

| ID  | Requirement                             |
| --- | --------------------------------------- |
| FR1 | Create and store a customer order       |
| FR2 | Publish an order-created event          |
| FR3 | Process payment from the received event |
| FR4 | Update inventory after order processing |
| FR5 | Generate order status notification      |
| FR6 | Handle processing failure appropriately |

FR1 → Order Service
FR2 → Order Service → order-events
FR3 → Payment Service
FR4 → Inventory Service
FR5 → Order Status Notification
FR6 → Failure Handling
