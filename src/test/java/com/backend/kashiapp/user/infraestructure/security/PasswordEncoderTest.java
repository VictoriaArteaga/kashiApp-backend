package com.backend.kashiapp.user.infraestructure.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderTest {

    @Test
    void shouldHashPassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        String raw = "123456";
        String hashed = encoder.encode(raw);

        assertNotEquals(raw, hashed);
        assertTrue(encoder.matches(raw, hashed));
    }
}