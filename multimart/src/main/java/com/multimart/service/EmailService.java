package com.multimart.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            String subject = "Verify your email address";
            String content = "Please click the link below to verify your email address:\n\n"
                    + "http://localhost:8080/api/auth/verify-email?token=" + token;
            
            helper.setText(content, false);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("noreply@multimart.com");
            
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            String subject = "Reset your password";
            String content = "Please click the link below to reset your password:\n\n"
                    + "http://localhost:3000/reset-password?token=" + token;
            
            helper.setText(content, false);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("noreply@multimart.com");
            
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
