package com.vermeg.jirachatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public void sendVerificationCode(String to, String code) {
        if (!emailEnabled) {
            log.warn("Email sending is disabled. Verification code for {}: {}", to, code);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Jira Chatbot - Code de vérification");
            message.setText("Votre code de vérification est : " + code + "\n\nCe code expire dans 10 minutes.");

            mailSender.send(message);
            log.info("Verification code sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email");
        }
    }

    public void sendPasswordResetCode(String to, String code) {
        if (!emailEnabled) {
            log.warn("Email sending is disabled. Password reset code for {}: {}", to, code);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Jira Chatbot - Réinitialisation du mot de passe");
            message.setText("Votre code de réinitialisation de mot de passe est : " + code + "\n\nCe code expire dans 10 minutes.");

            mailSender.send(message);
            log.info("Password reset code sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email");
        }
    }
}
