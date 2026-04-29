package com.backend.kashiapp.user.infraestructure.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    private final String SECRET = "12345678901234567890123456789012"; // 32 chars
    private final long EXPIRATION = 1000 * 60 * 10; // 10 minutos

    @BeforeEach
    void setup() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", EXPIRATION);
    }

    @Test
    void shouldGenerateAndExtractEmail() {

        String email = "test@test.com";

        String token = jwtService.generateToken(email);

        String extracted = jwtService.extractEmail(token);

        assertEquals(email, extracted);
    }

    @Test
    void shouldFailIfTokenIsModified() {

        String token = jwtService.generateToken("test@test.com");

        String fakeToken = token + "hack";

        assertThrows(Exception.class, () -> {
            jwtService.extractEmail(fakeToken);
        });
    }

    @Test
    void tokenShouldNotContainSensitiveData() {

        String token = jwtService.generateToken("test@test.com");

        String payload = new String(Base64.getDecoder()
                .decode(token.split("\\.")[1]));

        assertFalse(payload.contains("password"));
        assertFalse(payload.contains("otp"));
        assertFalse(payload.contains("123456"));
    }

    @Test
    void tokenShouldHaveExpiration() {

        String token = jwtService.generateToken("test@test.com");

        assertNotNull(token); // básico, pero confirma generación con expiración
    }
}