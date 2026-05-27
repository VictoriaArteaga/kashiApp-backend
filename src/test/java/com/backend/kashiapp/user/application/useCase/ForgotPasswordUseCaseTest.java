package com.backend.kashiapp.user.application.useCase;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.ForgotPasswordRequestDTO;
import com.backend.kashiapp.user.application.dto.ForgotPasswordResponseDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.repository.PasswordResetTokenRepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.JpaUserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.PasswordResetTokenEntity;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.user.infraestructure.security.EmailService;

@DisplayName("ForgotPasswordUseCase - TESTS")
class ForgotPasswordUseCaseTest {

    private UserRepository userRepository;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private EmailService emailService;
    private JpaUserRepository jpaUserRepository;
    private ForgotPasswordUseCase forgotPasswordUseCase;

    private final String EMAIL = "test@example.com";
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        emailService = mock(EmailService.class);
        jpaUserRepository = mock(JpaUserRepository.class);

        forgotPasswordUseCase = new ForgotPasswordUseCase(
            userRepository,
            passwordResetTokenRepository,
            emailService,
            jpaUserRepository
        );
    }

    @Test
    @DisplayName("Debe enviar al correo registrado un token de restablecimiento")
    void shouldSendResetEmail() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jpaUserRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail(EMAIL);

        ForgotPasswordResponseDTO response = forgotPasswordUseCase.forgotPassword(request);

        // Verifica que se guardó el token
        verify(passwordResetTokenRepository).save(any(PasswordResetTokenEntity.class));
        // Verifica que se envió el email
        verify(emailService).sendPasswordResetEmail(eq(EMAIL), anyString());
        // Verifica el mensaje de respuesta
        assertNotNull(response);
        assertEquals("Recibio un enlace de reestablecimiento", response.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar una excepcion cuando el usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail(EMAIL);

        Exception ex = assertThrows(UserNotFoundException.class, () ->  forgotPasswordUseCase.forgotPassword(request));
        assertEquals("Usuario no encontrado", ex.getMessage());

        // No debe guardar token ni enviar email
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    @DisplayName("Debe eliminar un token antiguo antes de generar el nuevo")
    void shouldDeletePreviousTokenBeforeCreatingNew() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(USER_ID);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jpaUserRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail(EMAIL);

        forgotPasswordUseCase.forgotPassword(request);

        // Verifica que elimina tokens previos antes de crear uno nuevo
        verify(passwordResetTokenRepository).deleteByUserId(USER_ID);
    }
}
