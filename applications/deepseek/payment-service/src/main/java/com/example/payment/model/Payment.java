package com.example.payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Payment {
    @Id
    private UUID id;
    private UUID orderId;
    private boolean success;
    private Instant createdAt;
}
