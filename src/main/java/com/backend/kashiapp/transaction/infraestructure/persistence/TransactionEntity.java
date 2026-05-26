package com.backend.kashiapp.transaction.infraestructure.persistence;

import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;



@Entity
@Table(name = "transacciones")
@Getter
@Setter
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // Vincula las dos entradas de auditoría (OUTGOING + INCOMING) de una misma transferencia
    @Column(name = "referencia_transferencia",
            nullable = false,
            columnDefinition = "uuid")
    private UUID transferReference;

    @Column(name = "billetera_origen_id",
            nullable = false,
            columnDefinition = "uuid")
    private UUID senderWalletId;

    @Column(name = "billetera_destino_id",
            nullable = false,
            columnDefinition = "uuid")
    private UUID receiverWalletId;

    // 15 dígitos con 2 decimales para manejar montos grandes.
    @Column(name = "monto",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado",
            nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo",
            nullable = false)
    private TransactionType type;

    @Column(name = "creado_at",
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

}
