package com.backend.kashiapp.wallet.domain.service.impl;


import com.backend.kashiapp.common.exception.WalletNotFoundException;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.application.mapper.WalletMapper;
import com.backend.kashiapp.wallet.domain.models.Wallet;
import com.backend.kashiapp.wallet.domain.repository.WalletRepository;
import com.backend.kashiapp.wallet.domain.service.WalletService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;


public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletByUserId(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
        .orElseThrow(() -> new WalletNotFoundException("No se encontró la billetera para la identificación: " + userId));
        return WalletMapper.toResponseDTO(wallet);
    }

    @Override
    @Transactional
    public WalletResponseDTO toggleVisibility(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new WalletNotFoundException("Billetera no encontrada para el usuario: " + userId));
        wallet.toggleVisibility();
        Wallet updated = walletRepository.save(wallet);
        return WalletMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void lockWalletsForUpdate(UUID userIdA, UUID userIdB) {
        // Ordenamos por UUID para tomar siempre los locks en el mismo orden global.
        // Así dos transferencias inversas (A->B y B->A) concurrentes no pueden esperar
        // cada una el lock que tiene la otra: se elimina el interbloqueo.
        boolean aFirst = userIdA.compareTo(userIdB) <= 0;
        UUID first = aFirst ? userIdA : userIdB;
        UUID second = aFirst ? userIdB : userIdA;

        lockWallet(first);
        if (!second.equals(first)) {
            lockWallet(second);
        }
    }

    // Adquiere el bloqueo de fila (SELECT ... FOR UPDATE). El lock se mantiene hasta el commit
    // de la transacción de la transferencia, serializando los envíos del mismo emisor para que
    // la verificación de duplicados sea fiable.
    private void lockWallet(UUID userId) {
        walletRepository.findByUserIdForUpdate(userId)
            .orElseThrow(() -> new WalletNotFoundException(
                "No se encontró la billetera. Usuario: " + userId));
    }

    @Override
    @Transactional
    public void updateBalance(UUID userId, BigDecimal amount) {
        // Ajuste atómico del saldo (saldo = saldo + amount) en una sola sentencia UPDATE.
        // Es inmune al lost update: dos transferencias concurrentes sobre la misma billetera
        // se serializan a nivel de fila y ninguna pisa el cambio de la otra.
        int updated = walletRepository.adjustBalance(userId, amount);
        if (updated == 0) {
            throw new WalletNotFoundException(
                "No se encontró la billetera para actualizar el saldo. Usuario: " + userId);
        }
    }
}