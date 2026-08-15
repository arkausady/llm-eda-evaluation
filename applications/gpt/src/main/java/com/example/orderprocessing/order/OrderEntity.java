package com.example.orderprocessing.order;

import com.example.orderprocessing.model.OrderItem;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
public class OrderEntity {
    @Id
    private UUID id;
    private String customerId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String statusMessage;
    private Instant createdAt;
    private Instant updatedAt;
    @Version
    private long version;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_lines")
    private List<OrderLineEmbeddable> items = new ArrayList<>();

    protected OrderEntity() {
    }

    public OrderEntity(String customerId, BigDecimal amount, List<OrderItem> orderItems) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.amount = amount;
        this.status = OrderStatus.CREATED;
        this.statusMessage = "Order created";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.items = orderItems.stream()
                .map(item -> new OrderLineEmbeddable(item.sku(), item.quantity()))
                .toList();
    }

    public void updateStatus(OrderStatus newStatus, String message) {
        this.status = newStatus;
        this.statusMessage = message;
        this.updatedAt = Instant.now();
    }

    public List<OrderItem> toOrderItems() {
        return items.stream().map(item -> new OrderItem(item.getSku(), item.getQuantity())).toList();
    }

    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public OrderStatus getStatus() { return status; }
    public String getStatusMessage() { return statusMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
