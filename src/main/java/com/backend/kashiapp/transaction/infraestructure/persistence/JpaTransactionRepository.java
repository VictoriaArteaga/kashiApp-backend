package com.backend.kashiapp.transaction.infraestructure.persistence;

import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Proporciona operaciones CRUD sobre la tabla de transacciones
public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    // Retorna los movimientos del usuario: OUTGOING cuando envió, INCOMING cuando recibió
    @Query(value = "SELECT t FROM TransactionEntity t WHERE " +
           "(t.senderWalletId = :walletId AND t.type = :outgoing) OR " +
           "(t.receiverWalletId = :walletId AND t.type = :incoming)",
           countQuery = "SELECT COUNT(t) FROM TransactionEntity t WHERE " +
           "(t.senderWalletId = :walletId AND t.type = :outgoing) OR " +
           "(t.receiverWalletId = :walletId AND t.type = :incoming)")
    Page<TransactionEntity> findHistoryByWalletId(
            @Param("walletId") UUID walletId,
            @Param("outgoing") TransactionType outgoing,
            @Param("incoming") TransactionType incoming,
            Pageable pageable
    );

    // Detecta una transferencia idéntica reciente (mismo emisor, destinatario y monto)
    // para descartar duplicados generados por el doble clic en "Enviar".
    @Query("SELECT COUNT(t) > 0 FROM TransactionEntity t WHERE " +
           "t.senderWalletId = :senderWalletId AND " +
           "t.receiverWalletId = :receiverWalletId AND " +
           "t.amount = :amount AND " +
           "t.type = :type AND " +
           "t.createdAt >= :since")
    boolean existsRecentDuplicate(
            @Param("senderWalletId") UUID senderWalletId,
            @Param("receiverWalletId") UUID receiverWalletId,
            @Param("amount") BigDecimal amount,
            @Param("type") TransactionType type,
            @Param("since") OffsetDateTime since
    );
}
