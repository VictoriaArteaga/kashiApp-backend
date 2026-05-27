package com.backend.kashiapp.user.infraestructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    Optional <PasswordResetTokenEntity> findByToken (String token);
    void deleteByUserId(UUID userID);

}
