package com.example.ecommerce.entity;

import com.example.ecommerce.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String paymentMethodId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected CustomerOrder() {
    }

    public CustomerOrder(UUID id, String customerEmail, String sku, int quantity, BigDecimal unitPrice,
                         BigDecimal totalAmount, String paymentMethodId) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.paymentMethodId = paymentMethodId;
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void updateStatus(OrderStatus status, String failureReason) {
        this.status = status;
        this.failureReason = failureReason;
        this.updatedAt = Instant.now();
    }

    public boolean isTerminal() {
        return status == OrderStatus.COMPLETED
                || status == OrderStatus.PAYMENT_FAILED
                || status == OrderStatus.INVENTORY_FAILED
                || status == OrderStatus.EVENT_PUBLICATION_FAILED;
    }

    public UUID getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public OrderStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
