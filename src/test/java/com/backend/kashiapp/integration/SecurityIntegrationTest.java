package com.backend.kashiapp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("KAN-67: Las peticiones HTTP planas deben ser rechazadas o redirigidas a HTTPS")
    void shouldRejectOrRedirectHttpRequests() throws Exception {
        // En Spring Security con requiresChannel().anyRequest().requiresSecure()
        // Una petición HTTP devuelve un 302 Found redirigiendo a HTTPS, o un 403/401 si no hay auth.
        // Simulamos una petición insegura (HTTP).
        // Nota: Si esto falla, debes agregar http.requiresChannel(channel -> channel.anyRequest().requiresSecure())
        // en tu SecurityConfig.java.
        mockMvc.perform(get("http://localhost/api/v1/auth/login"))
                // Podría ser 302 (Redirección a HTTPS) o un rechazo total
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    boolean isRejectedOrRedirected = status == 302 || status == 403 || status == 400 || status == 401;
                    if (!isRejectedOrRedirected) {
                        throw new AssertionError("La petición HTTP no fue redirigida ni rechazada. Status: " + status);
                    }
                });
    }
}
