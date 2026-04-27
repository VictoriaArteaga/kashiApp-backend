package com.backend.kashiapp.user.infraestructure.security;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.backend.kashiapp.common.exception.GlobalHeaderException;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOptEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("Tu token de autenticacion");
            helper.setText("Tu token de autenticacion es: " + token, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new GlobalHeaderException("Error al enviar el correo: " + e.getMessage());
        }
    }
}