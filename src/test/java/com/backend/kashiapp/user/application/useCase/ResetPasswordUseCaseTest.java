package com.backend.kashiapp.user.application.useCase;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.kashiapp.common.exception.InvalidTokenException;
import com.backend.kashiapp.user.application.dto.ResetPasswordRequestDTO;
import com.backend.kashiapp.user.application.dto.ResetPasswordResponseDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.repository.PasswordResetTokenRepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.PasswordResetTokenEntity;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;

@DisplayName("ResetPasswordUseCase - Tests")
class ResetPasswordUseCaseTest {

    private UserRepository userRepository;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private PasswordEncoder passwordEncoder;
    private ResetPasswordUseCase resetPasswordUseCase;

    private final UUID USER_ID = UUID.randomUUID();
    private final String TOKEN = "valid-token-123";
    private final String NEW_PASSWORD = "newPassword123";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        resetPasswordUseCase = new ResetPasswordUseCase(
            userRepository,
            passwordResetTokenRepository,
            passwordEncoder
        );
    }

    // Crear token válido
    private PasswordResetTokenEntity buildValidToken() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(USER_ID);

        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setToken(TOKEN);
        token.setUser(userEntity);
        token.setExpiresAt(OffsetDateTime.now().plusMinutes(15)); // no expirado
        return token;
    }

    @Test
    @DisplayName("Debe restablecer la contraseña exitosamente con token válido")
    void shouldResetPasswordSuccessfully() {
        PasswordResetTokenEntity resetToken = buildValidToken();

        User user = new User();
        user.setId(USER_ID);

        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(resetToken));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("hashedNewPassword");

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(TOKEN, NEW_PASSWORD);

        ResetPasswordResponseDTO response = resetPasswordUseCase.restablecerContraseña(request);

        assertEquals("Contraseña actualizada exitosamente", response.getMessage());
        assertEquals("hashedNewPassword", user.getPasswordHash());
        //Guardar la contraseña cambiada
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(resetToken);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el token no existe")
    void shouldThrowExceptionWhenInvaliToken() {
        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.empty());

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(TOKEN, NEW_PASSWORD);

        Exception ex = assertThrows(InvalidTokenException.class, () ->
            resetPasswordUseCase.restablecerContraseña(request));
        assertEquals("Token invalido", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción y eliminar el token cuando ha expirado")
    void shouldThrowExceptionWhenTokenIsExpired() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(USER_ID);

        PasswordResetTokenEntity expiredToken = new PasswordResetTokenEntity();
        expiredToken.setToken(TOKEN);
        expiredToken.setUser(userEntity);
        expiredToken.setExpiresAt(OffsetDateTime.now().minusMinutes(1)); 

        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(expiredToken));

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(TOKEN, NEW_PASSWORD);

        Exception ex = assertThrows(InvalidTokenException.class, () ->
            resetPasswordUseCase.restablecerContraseña(request));
        assertEquals("El token ha expirado", ex.getMessage());
        //Eliminar el token expirado
        verify(passwordResetTokenRepository).delete(expiredToken); 
        verify(userRepository, never()).save(any());
    }
}