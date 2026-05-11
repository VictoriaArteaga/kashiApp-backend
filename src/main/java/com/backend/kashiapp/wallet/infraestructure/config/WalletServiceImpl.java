package com.backend.kashiapp.wallet.infraestructure.config;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.application.mapper.WalletMapper;
import com.backend.kashiapp.wallet.domain.models.Wallet;
import com.backend.kashiapp.wallet.domain.repository.WalletRepository;
import com.backend.kashiapp.wallet.domain.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletByUserId(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException(
                "Wallet not found for user: " + userId));
        return WalletMapper.toResponseDTO(wallet);
    }

    @Override
    @Transactional
    public WalletResponseDTO toggleVisibility(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException(
                "Wallet not found for user: " + userId));
        wallet.toggleVisibility();
        Wallet updated = walletRepository.save(wallet);
        return WalletMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void updateBalance(UUID userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException(
                "Wallet not found for user: " + userId));
        wallet.applyBalanceChange(amount);
        walletRepository.save(wallet);
    }
}