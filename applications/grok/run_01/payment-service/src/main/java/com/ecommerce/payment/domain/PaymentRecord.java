package com.ecommerce.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentRecord {

    private final String orderId;
    private final String customerId;
    private final BigDecimal amount;
    private final String status;
    private final String transactionId;
    private final String reason;
    private final Instant processedAt;

    public PaymentRecord(
            String orderId,
            String customerId,
            BigDecimal amount,
            String status,
            String transactionId,
            String reason,
            Instant processedAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.transactionId = transactionId;
        this.reason = reason;
        this.processedAt = processedAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
