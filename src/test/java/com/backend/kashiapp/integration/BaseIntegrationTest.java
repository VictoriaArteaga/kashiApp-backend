package com.backend.kashiapp.integration;

import com.backend.kashiapp.TestcontainersConfiguration;
import com.backend.kashiapp.transaction.infraestructure.persistence.JpaTransactionRepository;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.JpaUserRepository;
import com.backend.kashiapp.user.infraestructure.security.JwtService;
import com.backend.kashiapp.wallet.infraestructure.persistence.JpaWalletRepository;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base común para los tests de integración: levanta el contexto completo de Spring con
 * MockMvc y un PostgreSQL real (Testcontainers), e incluye utilidades para sembrar usuarios
 * y billeteras. Centraliza lo que antes se duplicaba en cada @BeforeEach.
 *
 * Los usuarios se crean mediante el repositorio de dominio ({@link UserRepository}) y se
 * borran con el {@link JpaUserRepository} (la interfaz de dominio no expone deleteAll).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JpaUserRepository jpaUserRepository;

    @Autowired
    protected JpaWalletRepository walletRepository;

    @Autowired
    protected JpaTransactionRepository transactionRepository;

    @Autowired
    protected JwtService jwtService;

    // Borra transacciones, billeteras y usuarios para empezar cada test desde un estado limpio.
    protected void cleanDatabase() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        jpaUserRepository.deleteAll();
    }

    // Crea y persiste un usuario ACTIVE con un hash de contraseña de marcador de posición.
    // Suficiente para los tests que se autentican generando el JWT directamente con JwtService.
    protected User persistActiveUser(String email, String username, String phone, String idNumber) {
        return persistUser(email, username, phone, idNumber, "hash123", AccountStatus.ACTIVE);
    }

    // Variante que permite fijar el hash de contraseña (p. ej. un BCrypt real) y el estado.
    protected User persistUser(String email, String username, String phone,
                               String idNumber, String passwordHash, AccountStatus status) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setUsername(username);
        user.setNumberPhone(phone);
        user.setAccountStatus(status);
        user.setIdentificationNumber(idNumber);
        user.setCreationDate(OffsetDateTime.now());
        // 'intentos_fallidos' es NOT NULL en la tabla usuarios: inicializamos en 0.
        user.setFailedAttempts(0);
        return userRepository.save(user);
    }

    // Crea y persiste una billetera visible con el saldo indicado para el usuario dado.
    protected WalletEntity persistWallet(UUID userId, BigDecimal balance) {
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(userId);
        wallet.setBalance(balance);
        wallet.setVisible(true);
        return walletRepository.save(wallet);
    }
}
