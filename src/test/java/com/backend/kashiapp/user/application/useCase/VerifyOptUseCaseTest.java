package com.backend.kashiapp.user.application.useCase;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.repository.Token2FARepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.Token2FAEntity;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.user.infraestructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
@DisplayName("VerifyOptUseCase - TESTS")
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
    @DisplayName("Debe retornar JWT cuando el OTP es correcto")
    void shouldReturnJwtWhenOtpIsCorrect() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail(EMAIL);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(EMAIL);

        Token2FAEntity token = new Token2FAEntity();
        token.setUser(userEntity);
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
    @DisplayName("Debe lanzar excepción cuando el OTP es incorrecto")
    void shouldThrowExceptionWhenOtpIsIncorrect() {

        // El token en DB es diferente al ingresado por el usuario.
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail(EMAIL);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(EMAIL);

        Token2FAEntity token = new Token2FAEntity();
        token.setUser(userEntity);
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
    @DisplayName("Debe lanzar excepción cuando el OTP ha expirado")
    void shouldThrowExceptionWhenOtpIsExpired() {

        // El token existe, pero expiró

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail(EMAIL);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(EMAIL);

        Token2FAEntity token = new Token2FAEntity();
        token.setUser(userEntity);
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
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {

        // Simulación si el correo no esta registrado en el sistema.

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Exception ex = assertThrows(UserNotFoundException.class, () -> {
            verifyOptUseCase.verifyOpt(EMAIL, OTP);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("usuario"));
    }
}