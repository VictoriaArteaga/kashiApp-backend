package com.backend.kashiapp.wallet.domain.repository;

import com.backend.kashiapp.wallet.domain.models.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Optional<Wallet> findByUserId(UUID userId);
    Wallet save(Wallet wallet);
}
