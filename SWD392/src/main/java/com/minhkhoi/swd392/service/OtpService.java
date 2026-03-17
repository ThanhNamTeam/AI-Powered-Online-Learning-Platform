package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.entity.OtpVerification;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;
    // SecureRandom thay vì Random — an toàn hơn cho OTP auth
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(String email) {
        String otpCode = generateOtpCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        OtpVerification otp = OtpVerification.builder()
                .email(email)
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .isVerified(false)
                .build();

        otpRepository.save(otp);
        emailService.sendOtpEmail(email, otpCode);

        log.info("[OTP] Generated and sent OTP to: {}", email);
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        OtpVerification otp = otpRepository
                .findByEmailAndOtpCodeAndIsVerifiedFalseAndExpiresAtAfter(
                        email, otpCode, LocalDateTime.now())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Invalid or expired OTP"));

        otp.setIsVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);

        log.info("[OTP] Verified successfully for: {}", email);
        return true;
    }

    public boolean isEmailVerified(String email) {
        return otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .map(OtpVerification::getIsVerified)
                .orElse(false);
    }

    private String generateOtpCode() {
        // 100000 → 999999 (6 digits) — dùng SecureRandom
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Tự động dọn OTP hết hạn mỗi 30 phút.
     * Cần @EnableScheduling trên Swd392Application.
     */
    @Scheduled(fixedDelay = 1800000) // mỗi 30 phút
    @Transactional
    public void cleanupExpiredOtps() {
        int deleted = otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("[OTP] Cleaned up {} expired OTP(s)", deleted);
        }
    }
}
