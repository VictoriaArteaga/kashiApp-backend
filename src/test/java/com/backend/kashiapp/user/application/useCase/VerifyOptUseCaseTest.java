package com.backend.kashiapp.user.application.useCase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.domain.repository.Token2FARepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.Token2FAEntity;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.user.infraestructure.security.JwtService;

class VerifyOptUseCaseTest {

    private UserRepository userRepository;
    private Token2FARepository token2FARepository;
    private JwtService jwtService;

    private VerifyOptUseCase verifyOptUseCase;

    private final String EMAIL = "test@test.com";
    private final String OTP = "123456";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        token2FARepository = mock(Token2FARepository.class);
        jwtService = mock(JwtService.class);

        verifyOptUseCase = new VerifyOptUseCase(
                userRepository,
                token2FARepository,
                jwtService
        );
    }

    @Test
    void shouldReturnJwtWhenOtpIsCorrect() {

        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail(EMAIL);

        Token2FAEntity token = new Token2FAEntity();
        token.setUser(user);
        token.setToken(OTP);
        token.setExpirationTime(OffsetDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(token2FARepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(jwtService.generateToken(EMAIL)).thenReturn("jwt-token");

        // El usuario ingresa el código correcto.
        AuthResponseDTO response = verifyOptUseCase.verifyOpt(EMAIL, OTP);

        // Se emite el JWT de acceso.
        assertEquals("jwt-token", response.getToken());

        verify(token2FARepository).delete(token); // OTP eliminado después de uso.
    }

    @Test
    void shouldThrowExceptionWhenOtpIsIncorrect() {

        // El token en DB es diferente al ingresado por el usuario.
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail(EMAIL);

        Token2FAEntity token = new Token2FAEntity();
        token.setUser(user);
        token.setToken("654321");
        token.setExpirationTime(OffsetDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(token2FARepository.findByUserId(userId)).thenReturn(Optional.of(token));

        Exception ex = assertThrows(RuntimeException.class, () -> {
            verifyOptUseCase.verifyOpt(EMAIL, OTP);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("incorrecto"));

        verify(token2FARepository, never()).delete(token); // no elimina si es incorrecto.
    }

    @Test
    void shouldThrowExceptionWhenOtpIsExpired() {

        // El token existe, pero expiró

        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail(EMAIL);

        Token2FAEntity token = new Token2FAEntity();
        token.setUser(user);
        token.setToken(OTP);
        token.setExpirationTime(OffsetDateTime.now().minusMinutes(1)); // expirado.

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(token2FARepository.findByUserId(userId)).thenReturn(Optional.of(token));

        Exception ex = assertThrows(RuntimeException.class, () -> {
            verifyOptUseCase.verifyOpt(EMAIL, OTP);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("expirado"));

        verify(token2FARepository).delete(token); // elimina si expiró.
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        // Simulación si el correo no esta registrado en el sistema.

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () -> {
            verifyOptUseCase.verifyOpt(EMAIL, OTP);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("usuario"));
    }
}