package com.backend.kashiapp.integration;

import com.backend.kashiapp.user.application.dto.LoginRequestDTO;
import com.backend.kashiapp.user.application.dto.VerifyOptRequestDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.Token2FARepository;
import com.backend.kashiapp.user.infraestructure.security.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Camino feliz de autenticación: login -> OTP -> verify-otp -> JWT -> endpoint protegido")
public class AuthIntegrationTest extends BaseIntegrationTest {

    // Evita que el login intente enviar un correo real al generar el OTP.
    @MockBean
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Token2FARepository token2FARepository;

    private static final String EMAIL = "auth_user@test.com";
    private static final String PASSWORD = "Password123!";

    private User user;

    @BeforeEach
    void setUp() {
        // El token 2FA referencia al usuario por FK: hay que borrarlo antes que a los usuarios.
        token2FARepository.deleteAll();
        cleanDatabase();
        doNothing().when(emailService).sendOptEmail(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());

        user = persistUser(EMAIL, "auth_user", "3001234567", "ID-AUTH",
                passwordEncoder.encode(PASSWORD), AccountStatus.ACTIVE);
        persistWallet(user.getId(), new BigDecimal("250.00"));
    }

    @Test
    @DisplayName("Login con credenciales válidas genera y envía un OTP")
    void loginWithValidCredentialsSendsOtp() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("OTP enviado a tu correo electronico."));

        // Se debió generar un OTP persistido y haberse intentado enviar por correo.
        assertThat(token2FARepository.findByUserId(user.getId())).isPresent();
        verify(emailService).sendOptEmail(org.mockito.ArgumentMatchers.eq(EMAIL),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Login incorrecto es rechazado con 401 y no genera OTP")
    void loginWithWrongPasswordIsRejected() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword("contraseña-incorrecta");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        assertThat(token2FARepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flujo completo: el JWT obtenido tras verificar el OTP da acceso a un endpoint protegido")
    void fullFlowYieldsUsableJwt() throws Exception {
        // 1. Login -> genera el OTP en BD.
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(EMAIL);
        loginRequest.setPassword(PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // 2. Recuperamos el OTP de la base de datos (en vez de leerlo del correo).
        String otp = token2FARepository.findByUserId(user.getId()).orElseThrow().getToken();

        // 3. Verificamos el OTP -> obtenemos el JWT.
        VerifyOptRequestDTO verifyRequest = new VerifyOptRequestDTO();
        verifyRequest.setEmail(EMAIL);
        verifyRequest.setOtp(otp);

        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String jwt = objectMapper.readTree(verifyResult.getResponse().getContentAsString())
                .get("token").asText();
        assertThat(jwt).isNotBlank();

        // 4. El OTP es de un solo uso: tras verificarlo debe quedar eliminado.
        assertThat(token2FARepository.findByUserId(user.getId())).isEmpty();

        // 5. El JWT debe dar acceso a un endpoint protegido.
        mockMvc.perform(get("/api/v1/wallet/balance")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(250.00));
    }

    @Test
    @DisplayName("Un OTP incorrecto no entrega JWT")
    void wrongOtpDoesNotYieldJwt() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(EMAIL);
        loginRequest.setPassword(PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        VerifyOptRequestDTO verifyRequest = new VerifyOptRequestDTO();
        verifyRequest.setEmail(EMAIL);
        verifyRequest.setOtp("000000-mal");

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();
                    if (code == 200) {
                        throw new AssertionError("Un OTP incorrecto no debe entregar un JWT. Status: " + code);
                    }
                });
    }
}
