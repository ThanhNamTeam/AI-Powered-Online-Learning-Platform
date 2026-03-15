package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.entity.OtpVerification;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;


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

        log.info("OTP generated and sent to email: {}", email);
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

        log.info("OTP verified successfully for email: {}", email);
        return true;
    }

    public boolean isEmailVerified(String email) {
        return otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .map(OtpVerification::getIsVerified)
                .orElse(false);
    }

    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate 6-digit number
        return String.valueOf(otp);
    }

    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired OTPs");
    }
}

