package com.backend.kashiapp.user.application.useCase;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.UserProfileResponseDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;

class GetUserUseCaseTest {
    private UserRepository userRepository;
    private GetUserUseCase getUserUseCase;

    private final String EMAIL = "test@example.com";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        getUserUseCase = new GetUserUseCase(userRepository);
    }

    @Test
    void shouldReturnUserProfile() {

        UUID userId = UUID.randomUUID();
        OffsetDateTime creationDate = OffsetDateTime.now();

        User user = new User ();
        user.setId(userId);
        user.setEmail(EMAIL);
        user.setUsername("testuser");
        user.setNumberPhone("3001234567");
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setCreationDate(creationDate);
        

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        UserProfileResponseDTO response = getUserUseCase.getCurrentUserProfile(EMAIL);

        //Se espera el perfil del usuario con los datos correctos
        assertEquals(userId, response.getId());
        assertEquals(EMAIL, response.getEmail());
        assertEquals("testuser", response.getUsername());
        assertEquals("3001234567", response.getNumberPhone());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());
        assertEquals(creationDate, response.getCreatedAt());

    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        //Se espera que se lance una excepción de usuario no encontrado
        Exception ex = assertThrows(UserNotFoundException.class, () -> getUserUseCase.getCurrentUserProfile(EMAIL));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }
}

