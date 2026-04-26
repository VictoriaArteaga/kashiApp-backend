package com.backend.kashiapp.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
@Repository
//interfaz que proporciona metodos para interacturar con la base de datos de usuarios
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // Encontrar un usuario por su correo electrónico
    Optional<UserEntity> findByEmail(String email);
    // Verificar si un correo electrónico ya está registrado
    boolean existsByEmail(String email);
    //Verficar si un numero de telefono ya esta registrado
    boolean existsByNumberPhone(String numberPhone);
    
}

