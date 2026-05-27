package com.backend.kashiapp.wallet.infraestructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface JpaWalletRepository extends JpaRepository<WalletEntity, UUID> {
    Optional<WalletEntity> findByUserId(UUID userId);

    // Bloqueo pesimista de escritura sobre la billetera (SELECT ... FOR UPDATE).
    // Se usa como compuerta de serialización: mientras una transferencia del mismo emisor
    // mantenga la fila bloqueada, las demás esperan. Así la verificación de duplicados
    // (doble clic) de la segunda petición ocurre DESPUÉS del commit de la primera.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.userId = :userId")
    Optional<WalletEntity> findByUserIdForUpdate(@Param("userId") UUID userId);

    // Actualización atómica del saldo a nivel de base de datos: "saldo = saldo + :amount".
    // Un único UPDATE lee y escribe la fila de forma atómica (el motor toma el lock de fila
    // mientras dura la sentencia), por lo que dos transferencias concurrentes sobre la misma
    // billetera no pueden pisarse (lost update). A diferencia del patrón leer-modificar-guardar,
    // no usa el valor cacheado en el contexto de persistencia, sino el valor vigente en la fila.
    @Modifying
    @Query("UPDATE WalletEntity w SET w.balance = w.balance + :amount, w.updatedAt = :timestamp " +
           "WHERE w.userId = :userId")
    int adjustBalance(@Param("userId") UUID userId,
                      @Param("amount") BigDecimal amount,
                      @Param("timestamp") OffsetDateTime timestamp);
}
