package com.minhkhoi.swd392.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your OTP Code for Account Registration");
            message.setText(buildOtpEmailContent(otpCode));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailContent(String otpCode) {
        return String.format("""
                Hello,
                
                Your OTP code for account registration is: %s
                
                This code will expire in 5 minutes.
                
                If you did not request this code, please ignore this email.
                
                Best regards,
                SWD392 Team
                """, otpCode);
    }

    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Welcome to SWD392!");
            message.setText(buildWelcomeEmailContent(fullName));

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    private String buildWelcomeEmailContent(String fullName) {
        return String.format("""
                Hello %s,
                
                Welcome to SWD392 Online Learning Platform!
                
                Your account has been successfully created and verified.
                You can now log in and start your learning journey.
                
                Best regards,
                SWD392 Team
                """, fullName);
    }
}

