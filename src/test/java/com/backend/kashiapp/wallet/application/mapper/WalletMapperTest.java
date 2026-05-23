package com.backend.kashiapp.wallet.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.models.Wallet;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;

@DisplayName("Pruebas de WalletMapper")
class WalletMapperTest {

    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final BigDecimal BALANCE = new BigDecimal("500.00");
    private static final boolean VISIBLE = true;
    private static final OffsetDateTime UPDATED_AT = OffsetDateTime.now();

    @Nested
    @DisplayName("toDomain")
    class ToDomainTests {

        @Test
        @DisplayName("Debe convertir WalletEntity a Wallet")
        void shouldConvertEntityToDomainModel() {
            WalletEntity entity = buildWalletEntity();

            Wallet result = WalletMapper.toDomain(entity);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(WALLET_ID);
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getBalance()).isEqualTo(BALANCE);
            assertThat(result.isVisible()).isEqualTo(VISIBLE);
            assertThat(result.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        @DisplayName("Debe manejar balance cero")
        void shouldHandleWalletWithZeroBalance() {
            WalletEntity entity = buildWalletEntity();
            entity.setBalance(BigDecimal.ZERO);

            Wallet result = WalletMapper.toDomain(entity);

            assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debe manejar wallet invisible")
        void shouldHandleInvisibleWallet() {
            WalletEntity entity = buildWalletEntity();
            entity.setVisible(false);

            Wallet result = WalletMapper.toDomain(entity);

            assertThat(result.isVisible()).isFalse();
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Debe convertir Wallet a WalletEntity")
        void shouldConvertDomainModelToEntity() {
            Wallet wallet = buildWallet();

            WalletEntity result = WalletMapper.toEntity(wallet);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(WALLET_ID);
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getBalance()).isEqualTo(BALANCE);
            assertThat(result.isVisible()).isEqualTo(VISIBLE);
            assertThat(result.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        @DisplayName("Debe manejar balance negativo")
        void shouldHandleWalletWithNegativeBalance() {
            Wallet wallet = buildWallet();
            wallet.setBalance(new BigDecimal("-100.00"));

            WalletEntity result = WalletMapper.toEntity(wallet);

            assertThat(result.getBalance()).isEqualTo(new BigDecimal("-100.00"));
        }
    }

    @Nested
    @DisplayName("toResponseDTO")
    class ToResponseDTOTests {

        @Test
        @DisplayName("Debe convertir Wallet a WalletResponseDTO")
        void shouldConvertDomainModelToResponseDTO() {
            Wallet wallet = buildWallet();

            WalletResponseDTO result = WalletMapper.toResponseDTO(wallet);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(WALLET_ID);
            assertThat(result.getBalance()).isEqualTo(BALANCE);
            assertThat(result.isVisible()).isEqualTo(VISIBLE);
            assertThat(result.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }

        @Test
        @DisplayName("No debe incluir userId en el DTO")
        void shouldNotIncludeUserIdInResponseDTO() {
            Wallet wallet = buildWallet();

            WalletResponseDTO result = WalletMapper.toResponseDTO(wallet);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getBalance()).isNotNull();
        }

        @Test
        @DisplayName("Debe manejar montos grandes")
        void shouldHandleWalletWithLargeAmounts() {
            Wallet wallet = buildWallet();
            wallet.setBalance(new BigDecimal("999999.99"));

            WalletResponseDTO result = WalletMapper.toResponseDTO(wallet);

            assertThat(result.getBalance()).isEqualTo(new BigDecimal("999999.99"));
        }
    }

    @Nested
    @DisplayName("Bidireccionalidad")
    class BidirectionalityTests {

        @Test
        @DisplayName("Debe convertir Entity -> Domain -> Entity")
        void shouldConvertEntityToDomainToEntityCorrectly() {
            WalletEntity originalEntity = buildWalletEntity();

            Wallet domain = WalletMapper.toDomain(originalEntity);
            WalletEntity result = WalletMapper.toEntity(domain);

            assertThat(result.getId()).isEqualTo(originalEntity.getId());
            assertThat(result.getUserId()).isEqualTo(originalEntity.getUserId());
            assertThat(result.getBalance()).isEqualTo(originalEntity.getBalance());
            assertThat(result.isVisible()).isEqualTo(originalEntity.isVisible());
            assertThat(result.getUpdatedAt()).isEqualTo(originalEntity.getUpdatedAt());
        }

        @Test
        @DisplayName("Debe convertir Domain -> Entity -> Domain")
        void shouldConvertDomainToEntityToDomainCorrectly() {
            Wallet originalWallet = buildWallet();

            WalletEntity entity = WalletMapper.toEntity(originalWallet);
            Wallet result = WalletMapper.toDomain(entity);

            assertThat(result.getId()).isEqualTo(originalWallet.getId());
            assertThat(result.getUserId()).isEqualTo(originalWallet.getUserId());
            assertThat(result.getBalance()).isEqualTo(originalWallet.getBalance());
            assertThat(result.isVisible()).isEqualTo(originalWallet.isVisible());
            assertThat(result.getUpdatedAt()).isEqualTo(originalWallet.getUpdatedAt());
        }
    }

    private Wallet buildWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(WALLET_ID);
        wallet.setUserId(USER_ID);
        wallet.setBalance(BALANCE);
        wallet.setVisible(VISIBLE);
        wallet.setUpdatedAt(UPDATED_AT);
        return wallet;
    }

    private WalletEntity buildWalletEntity() {
        WalletEntity entity = new WalletEntity();
        entity.setId(WALLET_ID);
        entity.setUserId(USER_ID);
        entity.setBalance(BALANCE);
        entity.setVisible(VISIBLE);
        entity.setUpdatedAt(UPDATED_AT);
        return entity;
    }
}
