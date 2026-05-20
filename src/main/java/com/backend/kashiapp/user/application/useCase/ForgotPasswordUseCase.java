package com.backend.kashiapp.user.application.useCase;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.ForgotPasswordRequestDTO;
import com.backend.kashiapp.user.application.dto.ForgotPasswordResponseDTO;
import com.backend.kashiapp.user.domain.repository.PasswordResetTokenRepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.JpaUserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.PasswordResetTokenEntity;
import com.backend.kashiapp.user.infraestructure.security.EmailService;

import jakarta.transaction.Transactional;


@Service
public class ForgotPasswordUseCase {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final JpaUserRepository jpaUserRepository;


    public ForgotPasswordUseCase(UserRepository userRepository, 
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService, 
            JpaUserRepository jpaUserRepository) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.jpaUserRepository = jpaUserRepository;
    }

    @Transactional
    public ForgotPasswordResponseDTO forgotPassword (ForgotPasswordRequestDTO request){
        var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        passwordResetTokenRepository.deleteByUserId(user.getId());
        
        //Crear un token random utilizando UUID
        String token = UUID.randomUUID().toString();

        var userEntity = jpaUserRepository.findById(user.getId())
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        //Crear entidad del token de restablecimiento
        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(token);
        passwordResetTokenEntity.setUser(userEntity);
        passwordResetTokenEntity.setExpiresAt(OffsetDateTime.now().plusMinutes(15));
        
        //Guardar el token en el repository
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        //Enviar mensaje mediante email service
        emailService.sendPasswordResetEmail(request.getEmail(), token);

        return new ForgotPasswordResponseDTO("Recibio un enlace de reestablecimiento");
    }

}