package com.backend.kashiapp.wallet.application.mapper;

import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.models.Wallet;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;

public class WalletMapper {
    
    public static Wallet toDomain(WalletEntity entity) {
        Wallet wallet = new Wallet();
        wallet.setId(entity.getId());
        wallet.setUserId(entity.getUserId());
        wallet.setBalance(entity.getBalance());
        wallet.setVisible(entity.isVisible());
        wallet.setUpdatedAt(entity.getUpdatedAt());
        return wallet;
    }
    public static WalletEntity toEntity(Wallet wallet) {
        WalletEntity entity = new WalletEntity();
        entity.setId(wallet.getId());
        entity.setUserId(wallet.getUserId());
        entity.setBalance(wallet.getBalance());
        entity.setVisible(wallet.isVisible());
        entity.setUpdatedAt(wallet.getUpdatedAt());
        return entity;
    }

    public static WalletResponseDTO toResponseDTO(Wallet wallet) {
        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(wallet.getId());
        dto.setBalance(wallet.getBalance());
        dto.setVisible(wallet.isVisible());
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }
}
