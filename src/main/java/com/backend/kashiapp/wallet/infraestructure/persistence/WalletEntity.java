package com.backend.kashiapp.wallet.infraestructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "Billeteras")
@Getter
@Setter
public class WalletEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, unique = true,
            columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "saldo",nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "es_visible",nullable = false)
    private boolean visible;

    @Column(name = "actualizado_en", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = OffsetDateTime.now();
        if (this.balance == null) this.balance = BigDecimal.ZERO;
        this.visible = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
