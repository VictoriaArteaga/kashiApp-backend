package com.backend.kashiapp.transaction.infraestructure.adapter;

import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserQueryAdapter")
class UserQueryAdapterTest {

    private UserRepository userRepository;
    private UserQueryAdapter adapter;

    private static final String EMAIL = "user@test.com";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        adapter = new UserQueryAdapter(userRepository);
    }

    @Test
    @DisplayName("Debe devolver el UUID del usuario cuando el email existe")
    void shouldReturnUserIdWhenEmailExists() {
        UUID expectedId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setId(expectedId);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(entity));

        Optional<UUID> result = adapter.findUserIdByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertEquals(expectedId, result.get());
    }

    @Test
    @DisplayName("Debe devolver Optional vacío cuando el email no existe")
    void shouldReturnEmptyWhenEmailNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<UUID> result = adapter.findUserIdByEmail(EMAIL);

        assertTrue(result.isEmpty());
    }
}
