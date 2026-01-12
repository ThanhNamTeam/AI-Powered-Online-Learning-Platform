package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {

    Optional<OtpVerification> findByEmailAndOtpCodeAndIsVerifiedFalseAndExpiresAtAfter(
            String email, String otpCode, LocalDateTime currentTime);

    Optional<OtpVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByExpiresAtBefore(LocalDateTime currentTime);
}

