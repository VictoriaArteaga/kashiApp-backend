package com.backend.kashiapp.user.application.useCase;

import java.time.OffsetDateTime;
import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.kashiapp.common.exception.EmailAlreadyExistsException;
import com.backend.kashiapp.common.exception.PhoneNumberAlreadyExistsException;
import com.backend.kashiapp.user.application.dto.UserRequestDTO;
import com.backend.kashiapp.user.application.dto.UserResponseDTO;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;
import com.backend.kashiapp.wallet.infraestructure.persistence.JpaWalletRepository;


@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JpaWalletRepository jpaWalletRepository;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, JpaWalletRepository jpaWalletRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jpaWalletRepository = jpaWalletRepository;
    }
    
    //Metodo para registrar un nuevo usuario y validar que el correo electrónico no esté registrado previamente
    @Transactional
    public UserResponseDTO register(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado");
        }
        if (userRepository.existsByNumberPhone(request.getNumberPhone())) {
            throw new PhoneNumberAlreadyExistsException("El número de teléfono ya está registrado");
        }

        //creacion del usuario
        var user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername()); 
        user.setNumberPhone(request.getNumberPhone());
        user.setAccountStatus(AccountStatus.ACTIVE); 
        user.setIdentificationNumber(request.getIdentificationNumber());
        user.setCreationDate(OffsetDateTime.now());

        
        //guardar el usuario en la base de datos y capturar el usuario guardado para obtener su ID
        UserEntity savedUser = userRepository.save(user);
        

        // Crear la wallet automáticamente con saldo 0 ← nuevo
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(savedUser.getId());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setVisible(true);
        wallet.setUpdatedAt(OffsetDateTime.now());
        jpaWalletRepository.save(wallet);

        return new UserResponseDTO(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getUsername(),
            savedUser.getNumberPhone(),
            savedUser.getAccountStatus()
        );
    }

    

}
