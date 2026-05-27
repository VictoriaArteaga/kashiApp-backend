package com.backend.kashiapp.user.application.useCase;


import java.util.Optional;

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

import com.backend.kashiapp.common.exception.AccountDeletedException;
import com.backend.kashiapp.common.exception.InvalidCredentialsException;
import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.DeleteRequestDTO;
import com.backend.kashiapp.user.application.dto.DeleteResponseDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;
@DisplayName("DeleteUserUseCase - TESTS")
class DeleteUserUseCaseTest {

    private UserRepository userRepository;
    private DeleteUserUseCase deleteUserUseCase;
    private PasswordEncoder passwordEncoder;
    
    private final String EMAIL = "test@example.com";
    private final String PASSWORD = "Password123";
    private final String WRONG_PASSWORD = "wrongPassword";
    private final String PASSWORD_HASH = "hashedPassword";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        deleteUserUseCase = new DeleteUserUseCase(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Debe eliminar la cuenta con contraseña correcta")
    void shouldDeleteUserWithCorrectPassword() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(true);

        //El usuario ingresa la contraseña correcta
        DeleteRequestDTO request = new DeleteRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        
        //Se espera que el usuario sea eliminado
        DeleteResponseDTO response = deleteUserUseCase.deleteUser(request);
        
        assertEquals("Cuenta eliminada exitosamente", response.getMessage());
        assertEquals(AccountStatus.DELETED, user.getAccountStatus());

        verify(userRepository).save(user); //guardar el usuario con el nuevo estado
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la contraseña es incorrecta")
    void shouldThrowExceptionWithWrongPassword() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, user.getPasswordHash())).thenReturn(false);

        //El usuario ingresa la contraseña incorrecta
        DeleteRequestDTO request = new DeleteRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(WRONG_PASSWORD);

        //Se espera que se lance una excepción de credenciales inválidas
        Exception ex = assertThrows(InvalidCredentialsException.class, () -> deleteUserUseCase.deleteUser(request));
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus()); // Estado de cuenta: activo
        assertEquals("Contraseña incorrecta", ex.getMessage());
        verify(userRepository, never()).save(user); // Verificar que no se haya guardado el usuario
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta ya está eliminada")
    void shouldThrowExceptionWhenAccountAlreadyDeleted() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setAccountStatus(AccountStatus.DELETED);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        //El usuario intenta eliminar una cuenta que ya ha sido eliminada
        DeleteRequestDTO request = new DeleteRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        //Se espera que se lance una excepción de cuenta eliminada
        Exception ex = assertThrows(AccountDeletedException.class, () -> deleteUserUseCase.deleteUser(request));
        assertEquals(AccountStatus.DELETED, user.getAccountStatus()); // Estado de cuenta: eliminado
        assertEquals("La cuenta ya ha sido eliminada previamente", ex.getMessage());
        verify(userRepository, never()).save(any()); // Verificar que no se guarde ningún usuario
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        //El usuario intenta eliminar una cuenta que no existe
        DeleteRequestDTO request = new DeleteRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        //Se espera que se lance una excepción de usuario no encontrado
        Exception ex = assertThrows(UserNotFoundException.class, () -> deleteUserUseCase.deleteUser(request));
        assertEquals("Usuario no encontrado", ex.getMessage());
        verify(userRepository, never()).save(any()); // Verificar que no se haya guardado ningún usuario

    }
}
