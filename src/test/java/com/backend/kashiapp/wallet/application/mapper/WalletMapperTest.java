package com.backend.kashiapp.wallet.application.mapper;

import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.models.Wallet;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WalletMapper")
class WalletMapperTest {

    private final UUID id = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final BigDecimal balance = new BigDecimal("250.00");
    private final OffsetDateTime updatedAt = OffsetDateTime.now();

    @Test
    @DisplayName("Debe mapear todos los campos de la entidad al dominio")
    void shouldMapEntityToDomain() {
        WalletEntity entity = new WalletEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setBalance(balance);
        entity.setVisible(true);
        entity.setUpdatedAt(updatedAt);

        Wallet domain = WalletMapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getBalance()).isEqualByComparingTo(balance);
        assertThat(domain.isVisible()).isTrue();
        assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Debe mapear todos los campos del dominio a la entidad")
    void shouldMapDomainToEntity() {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setUserId(userId);
        wallet.setBalance(balance);
        wallet.setVisible(false);
        wallet.setUpdatedAt(updatedAt);

        WalletEntity entity = WalletMapper.toEntity(wallet);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getBalance()).isEqualByComparingTo(balance);
        assertThat(entity.isVisible()).isFalse();
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Debe mapear el dominio al DTO de respuesta sin exponer userId")
    void shouldMapDomainToResponseDTO() {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setUserId(userId);
        wallet.setBalance(balance);
        wallet.setVisible(true);
        wallet.setUpdatedAt(updatedAt);

        WalletResponseDTO dto = WalletMapper.toResponseDTO(wallet);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getBalance()).isEqualByComparingTo(balance);
        assertThat(dto.isVisible()).isTrue();
        assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Debe conservar todos los valores en un round trip dominio -> entidad -> dominio")
    void shouldPreserveAllValuesOnRoundTrip() {
        Wallet original = new Wallet();
        original.setId(id);
        original.setUserId(userId);
        original.setBalance(new BigDecimal("999.99"));
        original.setVisible(true);
        original.setUpdatedAt(updatedAt);

        Wallet roundTripped = WalletMapper.toDomain(WalletMapper.toEntity(original));

        assertThat(roundTripped.getId()).isEqualTo(original.getId());
        assertThat(roundTripped.getUserId()).isEqualTo(original.getUserId());
        assertThat(roundTripped.getBalance()).isEqualByComparingTo(original.getBalance());
        assertThat(roundTripped.isVisible()).isEqualTo(original.isVisible());
        assertThat(roundTripped.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }
}
