package com.backend.kashiapp.user.application.useCase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.domain.repository.UserRepository;

@Service
public class FailedAttemptService {

    private final UserRepository userRepository;

    public FailedAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(UUID userId) {
        var user = userRepository.findById(userId) 
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        
        //Incremenetar el contador de intentos fallidos
        user.recordFailedAttempt();
        userRepository.save(user); // Guardar los cambios en la base de datos después de registrar el intento fallido

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(UUID userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.resetFailedAttempts();
        userRepository.save(user); // Guardar los cambios en la base de datos después de rein
    }
}
