package com.backend.kashiapp.wallet.domain.repository;

import com.backend.kashiapp.wallet.domain.models.Wallet;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Optional<Wallet> findByUserId(UUID userId);

    // Lectura con bloqueo pesimista de escritura. Se usa como compuerta de serialización
    // de las transferencias de un mismo emisor (necesaria para la deduplicación de duplicados).
    Optional<Wallet> findByUserIdForUpdate(UUID userId);

    // Ajuste atómico del saldo (saldo = saldo + amount) a nivel de base de datos.
    // Evita las condiciones de carrera (lost update) entre transferencias concurrentes.
    // Devuelve el número de filas afectadas (0 si la billetera no existe).
    int adjustBalance(UUID userId, BigDecimal amount);

    Wallet save(Wallet wallet);
}
