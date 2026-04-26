package com.backend.kashiapp.user.infraestructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.backend.kashiapp.user.domain.models.enums.AccountStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Usuarios")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable=false, unique=true, length=255)
    private String email;

    @Column(nullable=false, name="password_hash")
    private String passwordHash;

    @Column(nullable=false, name="nombre_usuario")
    private String username;

    @Column(name="creado_at")
    private OffsetDateTime creationDate;

    @Column(nullable=false, unique=true, name="numero_telefono")
    private String numberPhone;

    @Enumerated(EnumType.STRING)
    @Column(length=20, name="estado_cuenta")
    private AccountStatus accountStatus;
}
