package com.backend.kashiapp.transaction.infraestructure.persistence;

import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// Proporciona operaciones CRUD sobre la tabla de transacciones
public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    // Retorna los movimientos del usuario: OUTGOING cuando envió, INCOMING cuando recibió
    @Query("SELECT t FROM TransactionEntity t WHERE " +
           "(t.senderWalletId = :walletId AND t.type = :outgoing) OR " +
           "(t.receiverWalletId = :walletId AND t.type = :incoming) " +
           "ORDER BY t.createdAt DESC")
    List<TransactionEntity> findHistoryByWalletId(
            @Param("walletId") UUID walletId,
            @Param("outgoing") TransactionType outgoing,
            @Param("incoming") TransactionType incoming
    );
}
