package com.backend.kashiapp.user.infraestructure.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderTest {

    @Test
    void shouldHashPassword() {

        // Instancia del codificador.
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "123456";

        // Gnerar el hash apartir de la contraseña en texto plano.
        String hashed = encoder.encode(raw);

        assertNotEquals(raw, hashed); // El hash no debe ser igual a la contraseña original.
        assertTrue(encoder.matches(raw, hashed));  // El encoder debe validar que el texto plano coincida con el hash.
    }
}