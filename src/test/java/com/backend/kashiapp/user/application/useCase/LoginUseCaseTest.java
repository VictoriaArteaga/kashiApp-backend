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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.kashiapp.common.exception.AccountDeletedException;
import com.backend.kashiapp.common.exception.AccountLockedException;
import com.backend.kashiapp.common.exception.InvalidCredentialsException;
import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.application.dto.LoginRequestDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.Token2FARepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.JpaUserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.user.infraestructure.security.EmailService;

@DisplayName("LoginUseCase - TESTS")
class LoginUseCaseTest {
    private LoginUseCase loginUseCase;
    private UserRepository userRepository;
    private FailedAttemptService failedAttemptService;
    private Token2FARepository token2FARepository;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;
    private JpaUserRepository jpaUserRepository;

    private final UUID USER_ID = UUID.randomUUID();
    private final String EMAIL = "test@example.com";
    private final String PASSWORD = "password";
    private final String PASSWORD_HASH = "passwordHash";
    private final String WRONG_PASSWORD = "wrongPassword";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        failedAttemptService = mock(FailedAttemptService.class);
        token2FARepository = mock(Token2FARepository.class);
        emailService = mock(EmailService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jpaUserRepository = mock(JpaUserRepository.class);

        loginUseCase = new LoginUseCase(
            userRepository,
            passwordEncoder,
            token2FARepository,
            emailService,
            failedAttemptService
            , jpaUserRepository);
    }

    private User buildUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        return user;
    }


    @Test
    @DisplayName("Debe iniciar sesión exitosamente y enviar OTP")
    public void shouldLoginSuccessfully(){
        //Crear un usuario activo
        User user = buildUser();
        // Crear un UserEntity para simular la consulta a la base de datos
        UserEntity userEntity = new UserEntity();

        userEntity.setId(USER_ID);
        userEntity.setEmail(EMAIL);
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(jpaUserRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));


        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        AuthResponseDTO response = loginUseCase.login(request);

        assertEquals("OTP enviado a tu correo electronico.", response.getToken());
        verify(token2FARepository).save(any());
        verify(failedAttemptService).resetFailedAttempts(USER_ID);  
        verify(emailService).sendOptEmail(eq(EMAIL), anyString());
    }

    @Test
    @DisplayName("Debe lanzar excepción y registrar intento fallido cuando la contraseña es incorrecta")
    public void shouldFailLoginWithWrongPassword(){
        User user = buildUser();
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH)).thenReturn(false);


        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(WRONG_PASSWORD);

        //crear excepcion de credenciales invalidas y verificar que se registre el intento fallido
        Exception ex = assertThrows(InvalidCredentialsException.class, () -> loginUseCase.login(request));
        assertEquals("Contraseña incorrecta", ex.getMessage());
        verify(failedAttemptService).recordFailedAttempt(USER_ID);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    public void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        Exception ex = assertThrows(UserNotFoundException.class, () -> loginUseCase.login(request));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta está eliminada")
    public void shouldThrowExceptionWhenAccountDeleted (){
        User user = buildUser();
        user.setAccountStatus(AccountStatus.DELETED);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        Exception ex = assertThrows(AccountDeletedException.class, () -> loginUseCase.login(request));
        assertEquals("La cuenta ha sido eliminada", ex.getMessage());
    }

    @Test 
    @DisplayName("Debe lanzar excepción cuando la cuenta está bloqueada")
    public void shouldThrowExceptionWhenAccountIsLocked() {
        User user = buildUser();
        user.setLockedUntil(OffsetDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        Exception ex = assertThrows(AccountLockedException.class, () -> loginUseCase.login(request));
        assertEquals("Cuenta bloqueada temporalmente debido a múltiples intentos fallidos. Intente nuevamente más tarde.", ex.getMessage());
    }

}