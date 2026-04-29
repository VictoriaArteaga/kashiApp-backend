package com.backend.kashiapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);

        String frontendUrl = getEnvVariable("URL_FRONTEND");

        if (frontendUrl == null || frontendUrl.isBlank()) {
            throw new IllegalArgumentException("Variable de entorno URL_FRONTEND no está configurada");
        }

        configuration.addAllowedOrigin(frontendUrl);
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String getEnvVariable(String variableName) {
        // Intenta primero desde variables de entorno del sistema
        String envValue = System.getenv(variableName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        // Si no está en variables de entorno, intenta leer del archivo .env
        try {
            Path envFile = Paths.get(".env");
            if (Files.exists(envFile)) {
                List<String> lines = Files.readAllLines(envFile);
                for (String line : lines) {
                    if (line.startsWith(variableName + "=")) {
                        return line.substring((variableName + "=").length()).trim();
                    }
                }
            }
        } catch (IOException e) {
            // Silenciar errores de lectura del archivo
        }

        return null;
    }
}

