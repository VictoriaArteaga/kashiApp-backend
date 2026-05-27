package com.backend.kashiapp.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.backend.kashiapp.user.domain.models.User;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNumberPhone(String numberPhone);
    User save(User user);
    Optional<User> findById(UUID id);
}

