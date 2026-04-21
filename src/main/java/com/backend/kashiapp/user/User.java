package com.backend.kashiapp.user;

import java.time.OffsetDateTime;

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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=255)
    private String email;

    @Column(nullable=false, name="password_hash")
    private String passwordHash;

    @Column(nullable=false, name="nombre_usuario")
    private String nombreUsuario;

    @Column(name= "creado_at")
    private OffsetDateTime creadoAt;

    @Enumerated(EnumType.STRING)
    @Column(length=20)
    private EstadoCuenta estado;

}
