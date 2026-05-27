package com.backend.kashiapp.user.application.useCase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.kashiapp.common.exception.InvalidTokenException;
import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.ResetPasswordRequestDTO;
import com.backend.kashiapp.user.application.dto.ResetPasswordResponseDTO;
import com.backend.kashiapp.user.domain.repository.PasswordResetTokenRepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.PasswordResetTokenEntity;

import jakarta.transaction.Transactional;

@Service
public class ResetPasswordUseCase {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordUseCase (UserRepository userRepository, 
            PasswordResetTokenRepository passwordResetTokenRepository, 
            PasswordEncoder passwordEncoder){
                this.userRepository = userRepository;
                this.passwordResetTokenRepository = passwordResetTokenRepository;
                this.passwordEncoder = passwordEncoder;
            }

    @Transactional
    public ResetPasswordResponseDTO restablecerContraseña (ResetPasswordRequestDTO request){
    
        PasswordResetTokenEntity token = passwordResetTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new InvalidTokenException("Token invalido"));

        if (token.isExpired()){
            passwordResetTokenRepository.delete(token);
            throw new InvalidTokenException ("El token ha expirado");
        }
        //Buscar el usuario asociado
        var user = userRepository.findById(token.getUser().getId())
            .orElseThrow(() -> new UserNotFoundException("Usuario no econtrado"));
        //
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        //Limpiar y eliminar token 
        passwordResetTokenRepository.delete(token);

        return new ResetPasswordResponseDTO("Contraseña actualizada exitosamente");

    }

}