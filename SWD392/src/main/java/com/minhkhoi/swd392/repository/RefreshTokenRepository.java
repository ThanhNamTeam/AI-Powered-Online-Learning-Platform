package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.RefreshToken;
import com.minhkhoi.swd392.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenAndRevokedFalseAndExpiresAtAfter(String token, LocalDateTime now);

}

