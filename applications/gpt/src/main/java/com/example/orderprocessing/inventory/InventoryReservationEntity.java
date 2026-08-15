package com.example.orderprocessing.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservationEntity {
    @Id
    private UUID orderId;
    private Instant reservedAt;

    protected InventoryReservationEntity() {
    }

    public InventoryReservationEntity(UUID orderId) {
        this.orderId = orderId;
        this.reservedAt = Instant.now();
    }
}
