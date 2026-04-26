package com.backend.kashiapp.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import com.backend.kashiapp.user.domain.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Encontrar un usuario por su correo electrónico
    Optional<User> findByEmail(String email);
    // Verificar si un correo electrónico ya está registrado
    boolean existsByEmail(String email);
    
}

