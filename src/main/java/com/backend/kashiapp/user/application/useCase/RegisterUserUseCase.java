package com.backend.kashiapp.user.application.useCase;

import org.springframework.stereotype.Service;
import com.backend.kashiapp.user.application.dto.UserRequestDTO;
import com.backend.kashiapp.user.application.dto.UserResponseDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;

import com.backend.kashiapp.common.exception.EmailAlreadyExistsException;
import com.backend.kashiapp.common.exception.PhoneNumberAlreadyExistsException;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    //Metodo para registrar un nuevo usuario y validar que el correo electrónico no esté registrado previamente
    public UserResponseDTO register(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado");
        }
        if (userRepository.existsByNumberPhone(request.getNumberPhone())) {
            throw new PhoneNumberAlreadyExistsException("El número de teléfono ya está registrado");
        }

        //creacion del usuario
        var user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername()); 
        user.setNumberPhone(request.getNumberPhone());
        user.setAccountStatus(AccountStatus.ACTIVE); 

        //guardar el usuario en la base de datos y capturar el usuario guardado para obtener su ID

        var savedUser = userRepository.save(user);
        return new UserResponseDTO(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getUsername(),
            savedUser.getNumberPhone(),
            savedUser.getAccountStatus()
        );
    }

}
