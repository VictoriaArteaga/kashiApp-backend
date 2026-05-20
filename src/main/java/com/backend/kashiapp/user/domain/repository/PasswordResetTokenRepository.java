package com.backend.kashiapp.user.domain.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.kashiapp.user.infraestructure.persistence.PasswordResetTokenEntity;

@Repository
public interface  PasswordResetTokenRepository {
    PasswordResetTokenEntity save(PasswordResetTokenEntity token);
    Optional <PasswordResetTokenEntity> findByToken(String token);
    void delete(PasswordResetTokenEntity token);
    void deleteByUserId(java.util.UUID userId);

}
