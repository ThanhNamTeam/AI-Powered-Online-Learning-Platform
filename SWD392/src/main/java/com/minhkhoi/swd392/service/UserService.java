package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.config.JwtUtil;
import com.minhkhoi.swd392.dto.request.CreateUserRequest;
import com.minhkhoi.swd392.dto.request.LoginRequest;
import com.minhkhoi.swd392.dto.request.RefreshTokenRequest;
import com.minhkhoi.swd392.dto.request.UpdateUserRequest;
import com.minhkhoi.swd392.dto.response.LoginResponse;
import com.minhkhoi.swd392.dto.response.UserResponse;
import com.minhkhoi.swd392.entity.OtpVerification;
import com.minhkhoi.swd392.entity.RefreshToken;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.RefreshTokenRepository;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.OtpVerificationRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    /**
     * Send OTP to email for registration
     */
    public void sendOtpForRegistration(String email) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, email);
        }

        // Generate 6-digit OTP
        String otpCode = generateOtpCode();

        // Calculate expiration time (5 minutes)
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Save OTP to database
        OtpVerification otp = OtpVerification.builder()
                .email(email)
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .isVerified(false)
                .build();

        otpRepository.save(otp);

        // Send OTP via email
        sendOtpEmail(email, otpCode);

        log.info("OTP generated and sent to email: {}", email);
    }

    /**
     * Create a new user with OTP verification
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, request.getEmail());
        }

        // Verify OTP
        OtpVerification otp = otpRepository
                .findByEmailAndOtpCodeAndIsVerifiedFalseAndExpiresAtAfter(
                        request.getEmail(), request.getOtpCode(), LocalDateTime.now())
                .orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID));

        // Mark OTP as verified
        otp.setIsVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);

        // Create user entity with default STUDENT role
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.STUDENT)
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // gửi mail cho nó thân thiện :))
        sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        log.info("User created successfully with email: {}", savedUser.getEmail());

        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Generate random 6-digit OTP code
     */
    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Send OTP email
     */
    private void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your OTP Code for Account Registration");
            message.setText(String.format("""
                Hello,
                
                Your OTP code for account registration is: %s
                
                This code will expire in 5 minutes.
                
                If you did not request this code, please ignore this email.
                
                Best regards,
                SWD392 Team-5
                """, otpCode));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new AppException(ErrorCode.OTP_SEND_FAILED);
        }
    }

    /**
     * Send welcome email
     */
    private void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Welcome to SWD392!");
            message.setText(String.format("""
                Hello %s,
                
                Welcome to SWD392 Online Learning Platform!
                
                Your account has been successfully created and verified.
                You can now log in and start your learning journey.
                
                Best regards,
                SWD392 Team-5
                """, fullName));

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    /**
     * Get user by ID
     */
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, userId));
        return UserResponse.fromEntity(user);
    }

    /**
     * Get user by email
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));
        return UserResponse.fromEntity(user);
    }

    /**
     * Get all users
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update nhưng mà mới làm cho admin
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, userId));

        // Update fields if provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if new email already exists for another user
            if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        // Save and return
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Authenticate user and generate JWT token
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Generate access token and refresh token
        String accessToken = jwtUtil.generateToken(user);
        String refreshTokenStr = jwtUtil.generateRefreshToken(user);

        // Save refresh token to database
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 days expiration
                .build();

        refreshTokenRepository.save(refreshToken);

        log.info("User logged in successfully: {}", user.getEmail());

        return new LoginResponse(accessToken, refreshTokenStr);
    }

    /**
     * Refresh access token using refresh token
     * Uses Rotating Refresh Token pattern for better security
     */
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // Validate refresh token from database
        RefreshToken oldRefreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndExpiresAtAfter(request.getRefreshToken(), LocalDateTime.now())
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        // Validate JWT token
        if (!jwtUtil.isTokenValid(request.getRefreshToken())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // Get user from refresh token
        User user = oldRefreshToken.getUser();

        // Revoke old refresh token
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);

        // Generate new access token and new refresh token
        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshTokenStr = jwtUtil.generateRefreshToken(user);

        // Save new refresh token to database
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenStr)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 days expiration
                .build();

        refreshTokenRepository.save(newRefreshToken);

        log.info("Access token and refresh token refreshed for user: {}", user.getEmail());

        return new LoginResponse(newAccessToken, newRefreshTokenStr);
    }
}
