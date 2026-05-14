package com.backend.kashiapp.wallet.infraestructure.config;

import com.backend.kashiapp.wallet.application.mapper.WalletMapper;
import com.backend.kashiapp.wallet.domain.models.Wallet;
import com.backend.kashiapp.wallet.domain.repository.WalletRepository;
import com.backend.kashiapp.wallet.domain.service.WalletService;
import com.backend.kashiapp.wallet.domain.service.impl.WalletServiceImpl;
import com.backend.kashiapp.wallet.infraestructure.persistence.JpaWalletRepository;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class WalletModuleConfig {

    @Bean
    public WalletRepository walletRepository(JpaWalletRepository jpaRepo) {
        return new WalletRepository() {

            @Override
            public Optional<Wallet> findByUserId(UUID userId) {
                return jpaRepo.findByUserId(userId)
                              .map(WalletMapper::toDomain);
            }

            @Override
            public Wallet save(Wallet wallet) {
                WalletEntity entity = WalletMapper.toEntity(wallet);
                WalletEntity saved = jpaRepo.save(entity);
                return WalletMapper.toDomain(saved);
            }
        };
    }

    @Bean
    public WalletService walletService(WalletRepository walletRepository) {
        return new WalletServiceImpl(walletRepository);
    }
}
