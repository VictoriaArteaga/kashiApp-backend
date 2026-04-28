package com.backend.kashiapp.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.kashiapp.user.infraestructure.persistence.Token2FAEntity;

@Repository
// Interfaz que define los métodos para interactuar con la base de datos de tokens 2FA
public interface Token2FARepository extends JpaRepository<Token2FAEntity, UUID> {
    Optional<Token2FAEntity> findByUserId(UUID userId);
}