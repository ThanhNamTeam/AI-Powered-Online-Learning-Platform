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
import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.repository.*;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
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

import java.math.BigDecimal;
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
    private final ProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;

    private final AISubscriptionRepository aiSubscriptionRepository;


    @Value("${app.frontend-url}")
    private String frontendUrl;
    public void sendOtpForRegistration(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, email);
        }

        String otpCode = generateOtpCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        OtpVerification otp = OtpVerification.builder()
                .email(email)
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .isVerified(false)
                .build();

        otpRepository.save(otp);
        sendOtpEmail(email, otpCode);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, request.getEmail());
        }

        if (request.getRole() != User.Role.STUDENT && request.getRole() != User.Role.INSTRUCTOR) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Only STUDENT and INSTRUCTOR roles are allowed for registration");
        }

        OtpVerification otp = otpRepository
                .findByEmailAndOtpCodeAndIsVerifiedFalseAndExpiresAtAfter(
                        request.getEmail(), request.getOtpCode(), LocalDateTime.now())
                .orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID));

        otp.setIsVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        AISubscription freeSub = AISubscription.builder()
                .instructor(savedUser)
                .plan(AISubscription.SubscriptionPlan.FREE)
                .status(AISubscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .aiCredits(0)
                .price(BigDecimal.valueOf(0))
                .endDate(LocalDateTime.now().plusYears(100))
                .build();

        aiSubscriptionRepository.save(freeSub);

        sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
        return userMapper.toUserResponse(savedUser);
    }

    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

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
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new AppException(ErrorCode.OTP_SEND_FAILED);
        }
    }

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
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, userId));
        UserResponse response = userMapper.toUserResponse(user);
        populateUserStats(response, user.getEmail());
        return response;
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));
        UserResponse response = userMapper.toUserResponse(user);
        populateUserStats(response, user.getEmail());
        return response;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, userId));


        userMapper.updateUserFromRequest(request, user);


        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, userId);
        }
        userRepository.deleteById(userId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public AuthTokenPair login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.generateToken(user);
        TokenPayload refreshPayload = jwtUtil.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshPayload.getToken())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthTokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshPayload.getToken())
                .build();
    }

    @Transactional
    public AuthTokenPair refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        RefreshToken oldRefreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndExpiresAtAfter(
                        refreshToken,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        User user = oldRefreshToken.getUser();
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);

        String newAccessToken = jwtUtil.generateToken(user);
        TokenPayload newRefreshPayload = jwtUtil.generateRefreshToken(user);

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


    public ValidateTokenResponse validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);

            if (jwtUtil.isTokenExpired(token)) {
                return ValidateTokenResponse.builder()
                        .valid(false)
                        .username(null)
                        .role(null)
                        .userId(null)
                        .message("Token has expired")
                        .build();
            }

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, username));

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

        redisTokenRepository.save(redisToken);
    }

    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));
        String token = UUID.randomUUID().toString();

        user.setResetPasswordToken(token);
        user.setTokenExpirationTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        String resetPasswordUrl = frontendUrl + "/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(email, resetPasswordUrl);

    }

    public void processResetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Invalid reset password token"));
        if (user.getTokenExpirationTime() == null || user.getTokenExpirationTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Reset password token has expired");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setTokenExpirationTime(null);
        userRepository.save(user);
    }

    public void updateAvatarUrl(String url) {
        var context = SecurityContextHolder.getContext();
        String email = Objects.requireNonNull(context.getAuthentication()).getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));
        user.setImageUrl(url);
        userRepository.save(user);
    }

    public void updateUserInfo(UpdateUserInfoRequest request) {
        var context = SecurityContextHolder.getContext();
        String email = Objects.requireNonNull(context.getAuthentication()).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        user.setFullName(request.getFullName());
        user.setBirthOfDate(request.getBirthOfDate());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setNotes(request.getNotes());
        user.setGender(request.getGender());

        userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        var context = SecurityContextHolder.getContext();
        String email = Objects.requireNonNull(context.getAuthentication()).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_OLD_PASSWORD);
        }

        if(request.getOldPassword().equals(request.getNewPassword())){
            throw new AppException(ErrorCode.EQUAL_PASSWORD);
        }

        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new AppException(ErrorCode.CONFIRM_PASSWORD_MISMATCH);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponse getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        String email = Objects.requireNonNull(context.getAuthentication()).getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        UserResponse response = UserResponse.fromEntity(user);
        populateUserStats(response, email);
        return response;
    }

    private void populateUserStats(UserResponse response, String email) {
        if (response.getRole() == User.Role.STUDENT) {
            long completedLessons = progressRepository.countCompletedLessonsByUserEmail(email);
            Long totalDurationSeconds = progressRepository.sumStudyTimeByUserEmail(email);

            response.setCompletedLessonsCount((int) completedLessons);
            double hours = (totalDurationSeconds != null) ? totalDurationSeconds / 3600.0 : 0.0;
            response.setStudyTimeHours(Math.round(hours * 10.0) / 10.0);
        } else if (response.getRole() == User.Role.STAFF) {
            response.setHandledCoursesCount(courseRepository.countByHandledByStaff_Email(email));
            
            response.setPendingModerationCount(courseRepository.countByStatusIn(List.of(
                CourseStatus.PENDING_APPROVAL,
                CourseStatus.PENDING_UPDATE,
                CourseStatus.PENDING_DELETION
            )));
        } else if (response.getRole() == User.Role.INSTRUCTOR) {
            response.setCreatedCoursesCount(courseRepository.countByConstructor_Email(email));
            response.setTotalStudentsCount(enrollmentRepository.countByCourse_Constructor_Email(email));
            
            BigDecimal revenue = paymentRepository.sumRevenueByInstructorEmail(email);
            response.setTotalRevenue(revenue != null ? revenue.doubleValue() : 0.0);
        }
    }

    @Transactional
    public UserResponse updateUserStats(UpdateUserStatsRequest request) {
        var context = SecurityContextHolder.getContext();
        String email = Objects.requireNonNull(context.getAuthentication()).getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        if (request.getXpDelta() != null && request.getXpDelta() > 0) {
            int newXp = (user.getCurrentXp() != null ? user.getCurrentXp() : 0) + request.getXpDelta();
            int level  = user.getLevel() != null ? user.getLevel() : 1;

            while (true) {
                int nextLevelXp = 1000 + (level - 1) * 500;
                if (newXp >= nextLevelXp) {
                    newXp -= nextLevelXp;
                    level++;
                } else {
                    break;
                }
            }
            user.setCurrentXp(newXp);
            user.setLevel(level);
        }

        if (request.getStreak() != null) {
            user.setStreak(request.getStreak());
        }

        if (request.getTotalBadges() != null) {
            user.setTotalBadges(request.getTotalBadges());
        }

        userRepository.save(user);

        UserResponse response = UserResponse.fromEntity(user);
        populateUserStats(response, email);
        return response;
    }

}
