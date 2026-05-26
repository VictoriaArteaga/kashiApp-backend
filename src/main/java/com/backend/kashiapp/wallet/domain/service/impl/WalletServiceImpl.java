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
    public void updateBalance(UUID userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new WalletNotFoundException("No se encontró la billetera para actualizar el saldo. Usuario: " + userId));
        wallet.applyBalanceChange(amount);
        walletRepository.save(wallet);
    }
}