package com.minhkhoi.swd392.service;

import com.fasterxml.jackson.databind.DatabindException;
import com.minhkhoi.swd392.config.JwtUtil;
import com.minhkhoi.swd392.dto.AuthTokenPair;
import com.minhkhoi.swd392.dto.JwtInfo;
import com.minhkhoi.swd392.dto.TokenPayload;
import com.minhkhoi.swd392.dto.request.*;
import com.minhkhoi.swd392.dto.response.LoginResponse;
import com.minhkhoi.swd392.dto.response.UserResponse;
import com.minhkhoi.swd392.dto.response.ValidateTokenResponse;
import com.minhkhoi.swd392.entity.OtpVerification;
import com.minhkhoi.swd392.entity.RedisToken;
import com.minhkhoi.swd392.entity.RefreshToken;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.RedisTokenRepository;
import com.minhkhoi.swd392.repository.RefreshTokenRepository;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.OtpVerificationRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import com.minhkhoi.swd392.mapper.UserMapper;
import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.*;
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
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final RedisTokenRepository redisTokenRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;
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

        // Validate role - only allow STUDENT or INSTRUCTOR for registration
        if (request.getRole() != User.Role.STUDENT && request.getRole() != User.Role.INSTRUCTOR) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Only STUDENT and INSTRUCTOR roles are allowed for registration");
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

        // Create user entity with role from request (mapped automatically)
        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        // Save user
        User savedUser = userRepository.save(user);

        // Send welcome email
        sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        log.info("User created successfully with email: {} and role: {}", 
            savedUser.getEmail(), savedUser.getRole());

        return userMapper.toUserResponse(savedUser);
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
        return userMapper.toUserResponse(user);
    }

    /**
     * Get user by email
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));
        return userMapper.toUserResponse(user);
    }

    /**
     * Get all users
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update nhưng mà mới làm cho admin
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, userId));

        // Update fields using Mapper
        userMapper.updateUserFromRequest(request, user);

        // Handle special fields
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

        // Save and return
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
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
    public AuthTokenPair login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Generate access token and refresh token
        String accessToken = jwtUtil.generateToken(user);
        TokenPayload refreshPayload = jwtUtil.generateRefreshToken(user);

        // Save refresh token to database
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshPayload.getToken())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 days expiration
                .build();
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthTokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshPayload.getToken())
                .build();
    }

    /**
     * Refresh access token using refresh token
     * Uses Rotating Refresh Token pattern for better security
     */
    @Transactional
    public AuthTokenPair refreshToken(String refreshToken) {

        //  Validate JWT trước
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }


        // Lookup DB bằng jti
        RefreshToken oldRefreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndExpiresAtAfter(
                        refreshToken,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        User user = oldRefreshToken.getUser();

        // Revoke refresh token cũ
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);

        //  Generate token mới
        String newAccessToken = jwtUtil.generateToken(user);
        TokenPayload newRefreshPayload = jwtUtil.generateRefreshToken(user);

        // Save refresh token mới
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshPayload.getToken())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return AuthTokenPair.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshPayload.getToken())
                .build();
    }


    /**
     * Validate access token
     */
    public ValidateTokenResponse validateToken(String token) {
        try {
            // Extract username (email) from token
            String username = jwtUtil.extractUsername(token);

            // Check if token is expired
            if (jwtUtil.isTokenExpired(token)) {
                return ValidateTokenResponse.builder()
                        .valid(false)
                        .username(null)
                        .role(null)
                        .userId(null)
                        .message("Token has expired")
                        .build();
            }

            // Find user by email
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, username));

            // Validate token with user details
            if (jwtUtil.isTokenValid(token, user)) {
                return ValidateTokenResponse.builder()
                        .valid(true)
                        .username(username)
                        .role(user.getRole().name())
                        .userId(UUID.fromString(user.getUserId()))
                        .message("Token is valid")
                        .build();
            } else {
                return ValidateTokenResponse.builder()
                        .valid(false)
                        .username(null)
                        .role(null)
                        .userId(null)
                        .message("Token is invalid")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error validating token", e);
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .username(null)
                    .role(null)
                    .userId(null)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }

    public void logout(String token){
        JwtInfo jwtInfo = jwtService.parseJwtInfo(token);
        String jwtId = jwtInfo.getJwtId();
        Date expiration = jwtInfo.getExpiredTime();
        if(expiration.before(new Date())){
            return;
        }
        RedisToken redisToken = RedisToken.builder()
                .jwtId(jwtId)
                .expiration(expiration.getTime() - System.currentTimeMillis())
                .build();

        log.info("SAVE REDIS JTI = {}", jwtInfo.getJwtId());
        redisTokenRepository.save(redisToken);
    }

    public void processForgotPassword(String email) {
        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));
        String token = UUID.randomUUID().toString();

        user.setResetPasswordToken(token);
        user.setTokenExpirationTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Send reset password email
        String resetPasswordUrl = frontendUrl + "/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(email, resetPasswordUrl);

    }

    public void processResetPassword(String token, String newPassword) {
        // Find user by reset password token
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Invalid reset password token"));

        // Check if token is expired
        if (user.getTokenExpirationTime() == null || user.getTokenExpirationTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Reset password token has expired");
        }

        // Update user's password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setTokenExpirationTime(null);
        userRepository.save(user);
    }

    public void updateAvatarUrl(String url) {

        // lấy userid từ token
        var context = SecurityContextHolder.getContext();
        String email = Objects.requireNonNull(context.getAuthentication()).getName();

        // tìm user
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        // cập nhật avatar
        user.setImageUrl(url);

        // lưu lại
        userRepository.save(user);
    }

    // NGƯỜI DÙNG TỰ UPDATE CHÍNH MÌNH
    public void updateUserInfo(UpdateUserInfoRequest request) {

        // lấy userid từ token
        var context = SecurityContextHolder.getContext();
        String email = Objects.requireNonNull(context.getAuthentication()).getName();

        // tìm user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        // cập nhật thông tin vào ENTITY
        user.setFullName(request.getFullName());
        user.setBirthOfDate(request.getBirthOfDate()); // LocalDate
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setNotes(request.getNotes());
        user.setGender(request.getGender());

        // lưu lại
        userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequest request) {

        // lấy userid từ token
        var context = SecurityContextHolder.getContext();
        String email = Objects.requireNonNull(context.getAuthentication()).getName();

        // tìm user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        // kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_OLD_PASSWORD);
        }

        if(request.getOldPassword().equals(request.getNewPassword())){
            throw new AppException(ErrorCode.EQUAL_PASSWORD);
        }

        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new AppException(ErrorCode.CONFIRM_PASSWORD_MISMATCH);
        }

        // cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        // lưu lại
        userRepository.save(user);
    }

}
