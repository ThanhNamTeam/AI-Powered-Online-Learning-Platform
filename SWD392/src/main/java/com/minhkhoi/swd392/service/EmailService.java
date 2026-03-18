package com.minhkhoi.swd392.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * EmailService sử dụng Brevo Transactional Email API (HTTP)
 * thay vì SMTP — tránh bị Render free tier block port 587/465.
 *
 * API docs: https://developers.brevo.com/reference/sendtransacemail
 */
@Service
@Slf4j
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    @Value("${brevo.sender-name:SWD392 Team}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    // ==================== OTP ====================

    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "Your OTP Code for Account Registration";
        String content = buildOtpEmailContent(otpCode);

        sendEmail(toEmail, subject, content);
        log.info("OTP email sent to: {}", toEmail);
    }

    // ==================== Welcome ====================

    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            String subject = "Welcome to AI Learning Platform!";
            String content = buildWelcomeEmailContent(fullName);
            sendEmail(toEmail, subject, content);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            // Không throw — welcome email không critical
        }
    }

    // ==================== Reset Password ====================

    public void sendResetPasswordEmail(String toEmail, String resetPasswordUrl) {
        String subject = "Yêu cầu đặt lại mật khẩu";
        String content = "Chào bạn,\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấn vào link bên dưới:\n"
                + resetPasswordUrl + "\n\n"
                + "Link này sẽ hết hạn sau 15 phút.\n"
                + "Nếu bạn không yêu cầu, vui lòng bỏ qua email này.";

        sendEmail(toEmail, subject, content);
        log.info("Reset password email sent to: {}", toEmail);
    }

    // ==================== Core: Brevo HTTP API ====================

    private void sendEmail(String toEmail, String subject, String textContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", subject,
                    "textContent", textContent
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    BREVO_API_URL, request, String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Brevo API returned: " + response.getStatusCode()
                        + " | " + response.getBody());
            }

            log.debug("Brevo API response: {} for {}", response.getStatusCode(), toEmail);

        } catch (Exception e) {
            log.error("Failed to send email via Brevo API to: {} | {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    // ==================== Email Templates ====================

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
