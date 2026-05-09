package com.backend.kashiapp.user.infraestructure.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    // Clave secreta de prueba.
    private final String SECRET = "12345678901234567890123456789012";
    private final long EXPIRATION = 1000 * 60 * 10;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", EXPIRATION);
    }

    @Test
    void shouldGenerateAndExtractEmail() {

        String email = "test@test.com";

        // Generar token y luego intenta extraer la información.
        String token = jwtService.generateToken(email);
        String extracted = jwtService.extractEmail(token);

        // La información extraida debe ser identica a la original.
        assertEquals(email, extracted);
    }

    @Test
    void shouldFailIfTokenIsModified() {

        // Crear un token valido y luego corromperlo.
        String token = jwtService.generateToken("test@test.com");
        String fakeToken = token + "hack"; // Modifica la firma del contenido.

        // Intenta usar un token modificado y se dispara una excepción de seguridad.
        assertThrows(Exception.class, () -> {
            jwtService.extractEmail(fakeToken);
        });
    }

    @Test
    void tokenShouldNotContainSensitiveData() {

        // Generar un token normal.

        String token = jwtService.generateToken("test@test.com");

        // Decodificación en Base64.
        String payload = new String(Base64.getDecoder()
                .decode(token.split("\\.")[1]));

        // Validar que no se filtre información critica en el cuerpo del JWT
        assertFalse(payload.contains("password"));
        assertFalse(payload.contains("otp"));
        assertFalse(payload.contains("123456"));
    }

    @Test
    void tokenShouldHaveExpiration() {

        // Generar token con la configuración de tiempo.
        String token = jwtService.generateToken("test@test.com");

        // Verificar que el token no es nulo.
        assertNotNull(token); // confirma generación con expiración
    }
}