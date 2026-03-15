package com.minhkhoi.swd392.config;

import com.minhkhoi.swd392.dto.TokenPayload;
import com.minhkhoi.swd392.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(User user) {

        String jti = UUID.randomUUID().toString();
        Date expiredAt = new Date(System.currentTimeMillis() + jwtExpiration);
        log.info("GENERATE JTI = {}", jti);

        return Jwts.builder()
                .id(jti)
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getUserId().toString())
                .issuedAt(new Date())
                .expiration(expiredAt)
                .signWith(getSigningKey())
                .compact();
    }

    public TokenPayload generateRefreshToken(User user) {
        String jti = UUID.randomUUID().toString();
        Date expiredAt = new Date(System.currentTimeMillis() + jwtRefreshExpiration);
        String token = Jwts.builder()
                .subject(user.getEmail())
                .id(jti)
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(expiredAt)
                .signWith(getSigningKey())
                .compact();


        return TokenPayload.builder()
                .token(token)
                .expiredTime(expiredAt)
                .jwtId(jti)
                .build();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return extractEmail(token);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            return (username.equals(user.getEmail())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
