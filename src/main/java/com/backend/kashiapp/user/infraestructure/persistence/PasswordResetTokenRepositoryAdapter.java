package com.backend.kashiapp.user.infraestructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.backend.kashiapp.user.domain.repository.PasswordResetTokenRepository;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final JpaPasswordResetTokenRepository jpaRepository;

    public PasswordResetTokenRepositoryAdapter(JpaPasswordResetTokenRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PasswordResetTokenEntity save(PasswordResetTokenEntity token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<PasswordResetTokenEntity> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public void delete(PasswordResetTokenEntity token) {
        jpaRepository.delete(token);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}