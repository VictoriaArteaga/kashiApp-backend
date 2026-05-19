package com.backend.kashiapp.user.application.useCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.kashiapp.common.exception.EmailAlreadyExistsException;
import com.backend.kashiapp.common.exception.PhoneNumberAlreadyExistsException;
import com.backend.kashiapp.user.application.dto.UserRequestDTO;
import com.backend.kashiapp.user.application.dto.UserResponseDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.wallet.infraestructure.persistence.JpaWalletRepository;

class RegisterUserUseCaseTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RegisterUserUseCase registerUserUseCase;
    private JpaWalletRepository jpaWalletRepository; 

    private final String EMAIL = "test@example.com";
    private final String PHONE = "2141242222";
    private final String PASSWORD = "password123";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jpaWalletRepository = mock(JpaWalletRepository.class);
        registerUserUseCase = new RegisterUserUseCase(userRepository, passwordEncoder, jpaWalletRepository);
    }

    @Test
    void shouldRegisterUser() {

        //Simulación del guardado del usuario 
        User savedUser = new User();
  
        savedUser.setId(java.util.UUID.randomUUID());
        savedUser.setEmail(EMAIL);
        savedUser.setUsername("testuser");
        savedUser.setNumberPhone(PHONE);
        savedUser.setAccountStatus(AccountStatus.ACTIVE);
        savedUser.setCreationDate(java.time.OffsetDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(passwordEncoder.encode(PASSWORD)).thenReturn("encryptedPassword");
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByNumberPhone(PHONE)).thenReturn(false);
        when(jpaWalletRepository.save(any())).thenReturn(null);
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setUsername("testuser");
        request.setNumberPhone(PHONE);
        request.setIdentificationNumber("1001001000");

        UserResponseDTO response = registerUserUseCase.register(request);

        //Se espera que el usuario sea registrado correctamente
        assertEquals(EMAIL, response.getEmail());
        assertEquals("testuser", response.getUsername());
        assertEquals(PHONE, response.getNumberPhone());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());

        //Se guarda el usuario en la base de datos con los datos correctos y se encripta la contraseña
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        UserRequestDTO request = new UserRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setUsername("testuser");
        request.setNumberPhone(PHONE);
        request.setIdentificationNumber("1001001000");

        //Se espera que se lance una excepción de correo electrónico ya registrado
        Exception ex = assertThrows(EmailAlreadyExistsException.class, () -> registerUserUseCase.register(request));
        assertEquals("El correo electrónico ya está registrado", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPhoneExits() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByNumberPhone(PHONE)).thenReturn(true);

        UserRequestDTO request = new UserRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setUsername("testuser");
        request.setNumberPhone(PHONE);
        request.setIdentificationNumber("1001001000");

        //Se espera que se lance una excepción de número de teléfono ya registrado
        Exception ex = assertThrows(PhoneNumberAlreadyExistsException.class, () -> registerUserUseCase.register(request));
        assertEquals("El número de teléfono ya está registrado", ex.getMessage());
    }

}