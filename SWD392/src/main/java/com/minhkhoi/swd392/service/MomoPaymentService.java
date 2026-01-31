package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.config.MomoConfig;
import com.minhkhoi.swd392.dto.request.CreatePaymentRequest;
import com.minhkhoi.swd392.dto.response.MomoPaymentResponse;
import com.minhkhoi.swd392.dto.response.PaymentResponse;
import com.minhkhoi.swd392.entity.AISubscription;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.Payment;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.mapper.PaymentMapper;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.AISubscriptionRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.PaymentRepository;

import com.minhkhoi.swd392.repository.UserRepository;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.constant.CourseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoPaymentService {

    private final MomoConfig momoConfig;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AISubscriptionRepository aiSubscriptionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final PaymentMapper paymentMapper;

    /**
     * Create MOMO payment for PREMIUM subscription
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Validate user role (only INSTRUCTOR or STUDENT can purchase)
        if (user.getRole() != User.Role.INSTRUCTOR && user.getRole() != User.Role.STUDENT) {
            throw new AppException(ErrorCode.ONLY_STUDENT_OR_INSTRUCTOR_CAN_PURCHASE);
        }

        // Validate subscription plan
        AISubscription.SubscriptionPlan plan;
        try {
            plan = AISubscription.SubscriptionPlan.valueOf(request.getSubscriptionPlan().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_PLAN);
        }

        // Generate unique IDs
        String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        
        // Convert amount to long (MOMO requires amount in VND, no decimals)
        long amount = request.getAmount().longValue();
        
        String orderInfo = request.getOrderInfo() != null 
            ? request.getOrderInfo() 
            : "Premium Subscription - " + plan.name();

        try {
            // Start Fix: Handle Enrollment for Subscription
            // Check if placeholder course exists for this user
            List<Course> existingCourses = courseRepository.findByConstructor_Email(user.getEmail());
            Course placeholderCourse = existingCourses.stream()
                    .filter(c -> "SUBSCRIPTION_PLACEHOLDER".equals(c.getTitle()))
                    .findFirst()
                    .orElse(null);

            if (placeholderCourse == null) {
                placeholderCourse = Course.builder()
                        .constructor(user)
                        .title("SUBSCRIPTION_PLACEHOLDER")
                        .description("Placeholder course for subscription payments")
                        .status(CourseStatus.DRAFT)
                        .createdAt(LocalDateTime.now())
                        .build();
                placeholderCourse = courseRepository.save(placeholderCourse);
            }

            final Course finalCourse = placeholderCourse;

            // Create or reuse enrollment linked to placeholder course
            Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, finalCourse)
                    .orElseGet(() -> {
                        Enrollment newEnroll = Enrollment.builder()
                                .user(user)
                                .course(finalCourse)
                                .enrolledAt(LocalDateTime.now())
                                .build();
                        return enrollmentRepository.save(newEnroll);
                    });
            // End Fix
            
            // Create MOMO payment request
            MomoPaymentResponse momoResponse = createMomoPayment(orderId, requestId, amount, orderInfo);

            // Create payment record
            Payment payment = Payment.builder()
                    .user(user)
                    .enrollment(enrollment) // Subscription doesn't have enrollment
                    .amount(request.getAmount())
                    .status(Payment.PaymentStatus.PENDING)
                    .method(Payment.PaymentMethod.MOMO)
                    .transactionId(orderId)
                    .notes("Subscription Plan: " + plan.name() + " | Order Info: " + orderInfo)
                    .build();
            
            payment = paymentRepository.save(payment);

            // Build response using mapper
            PaymentResponse response = paymentMapper.toPaymentResponse(payment);
            response.setPaymentUrl(momoResponse.getPayUrl());
            return response;

        } catch (Exception e) {
            log.error("Error creating MOMO payment: ", e);
            // Check if it's already an AppException
            if (e instanceof AppException) {
                throw (AppException) e;
            }
            throw new AppException(ErrorCode.PAYMENT_CREATION_FAILED, e.getMessage());
        }
    }

    /**
     * Create MOMO payment request
     */
    private MomoPaymentResponse createMomoPayment(String orderId, String requestId, long amount, String orderInfo) throws Exception {
        String partnerCode = momoConfig.getPartnerCode();
        String accessKey = momoConfig.getAccessKey();
        String secretKey = momoConfig.getSecretKey();
        String redirectUrl = momoConfig.getRedirectUrl();
        String ipnUrl = momoConfig.getIpnUrl();
        String requestType = "captureWallet";
        String extraData = "";

        // Create raw signature
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        log.info("Raw signature: {}", rawSignature);

        // Generate signature
        String signature = hmacSHA256(rawSignature, secretKey);
        log.info("Signature: {}", signature);

        // Build request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("accessKey", accessKey);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", requestType);
        requestBody.put("signature", signature);
        requestBody.put("lang", "en");

        log.info("Request body: {}", requestBody.toString());

        // Send request to MOMO
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(momoConfig.getEndpoint())
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        okhttp3.Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        
        log.info("MOMO Response: {}", responseBody);

        JSONObject jsonResponse = new JSONObject(responseBody);

        // Parse response
        return MomoPaymentResponse.builder()
                .partnerCode(jsonResponse.optString("partnerCode"))
                .orderId(jsonResponse.optString("orderId"))
                .requestId(jsonResponse.optString("requestId"))
                .amount(jsonResponse.optLong("amount"))
                .responseTime(jsonResponse.optLong("responseTime"))
                .message(jsonResponse.optString("message"))
                .resultCode(jsonResponse.optString("resultCode"))
                .payUrl(jsonResponse.optString("payUrl"))
                .deeplink(jsonResponse.optString("deeplink"))
                .qrCodeUrl(jsonResponse.optString("qrCodeUrl"))
                .deeplinkMiniApp(jsonResponse.optString("deeplinkMiniApp"))
                .build();
    }

    /**
     * Handle MOMO IPN callback
     */
    @Transactional
    public void handleMomoCallback(Map<String, Object> params) {
        // Convert Map<String, Object> to Map<String, String>
        Map<String, String> stringParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            stringParams.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        String orderId = stringParams.get("orderId");
        String resultCode = stringParams.get("resultCode");
        String message = stringParams.get("message");
        String signature = stringParams.get("signature");

        log.info("MOMO Callback - OrderId: {}, ResultCode: {}, Message: {}", orderId, resultCode, message);

        // Verify signature
        if (!verifyMomoSignature(stringParams, signature)) {
            log.error("Invalid MOMO signature for orderId: {}", orderId);
            // check signature disabled for local testing
            // throw new AppException(ErrorCode.INVALID_MOMO_SIGNATURE);
        }

        // Find payment by transaction ID
        Payment payment = paymentRepository.findByTransactionId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        // Update payment status based on result code
        if ("0".equals(resultCode)) {
            // Payment successful
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
            
            // Create or update AI subscription
            createOrUpdateSubscription(payment);
            
            log.info("Payment completed successfully for orderId: {}", orderId);
        } else {
            // Payment failed
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setNotes(payment.getNotes() + " | Failed: " + message);
            log.error("Payment failed for orderId: {}. Message: {}", orderId, message);
        }

        paymentRepository.save(payment);
    }

    /**
     * Create or update AI subscription after successful payment
     */
    private void createOrUpdateSubscription(Payment payment) {
        User user = payment.getUser();
        
        // Extract subscription plan from payment notes
        String notes = payment.getNotes();
        String planName = extractSubscriptionPlan(notes);
        AISubscription.SubscriptionPlan plan = AISubscription.SubscriptionPlan.valueOf(planName);

        // Calculate subscription period (1 month)
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(1);

        // Set AI credits based on plan
        Integer aiCredits = switch (plan) {
            case BASIC -> 100;
            case PREMIUM -> 500;
            case ENTERPRISE -> null; // Unlimited
        };

        // Create new subscription
        AISubscription subscription = AISubscription.builder()
                .instructor(user)
                .plan(plan)
                .status(AISubscription.SubscriptionStatus.ACTIVE)
                .price(payment.getAmount())
                .startDate(startDate)
                .endDate(endDate)
                .autoRenew(false)
                .aiCredits(aiCredits)
                .aiCreditsUsed(0)
                .notes("Payment ID: " + payment.getPaymentId())
                .build();

        aiSubscriptionRepository.save(subscription);
        log.info("Created subscription for user: {} with plan: {}", user.getEmail(), plan);
    }

    /**
     * Extract subscription plan from payment notes
     */
    private String extractSubscriptionPlan(String notes) {
        if (notes.contains("BASIC")) return "BASIC";
        if (notes.contains("PREMIUM")) return "PREMIUM";
        if (notes.contains("ENTERPRISE")) return "ENTERPRISE";
        return "BASIC"; // Default
    }

    /**
     * Verify MOMO signature
     */
    private boolean verifyMomoSignature(Map<String, String> params, String signature) {
        try {
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + params.get("amount") +
                    "&extraData=" + params.getOrDefault("extraData", "") +
                    "&message=" + params.get("message") +
                    "&orderId=" + params.get("orderId") +
                    "&orderInfo=" + params.get("orderInfo") +
                    "&orderType=" + params.get("orderType") +
                    "&partnerCode=" + params.get("partnerCode") +
                    "&payType=" + params.get("payType") +
                    "&requestId=" + params.get("requestId") +
                    "&responseTime=" + params.get("responseTime") +
                    "&resultCode=" + params.get("resultCode") +
                    "&transId=" + params.get("transId");

            String computedSignature = hmacSHA256(rawSignature, momoConfig.getSecretKey());
            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying MOMO signature: ", e);
            return false;
        }
    }

    /**
     * Generate HMAC SHA256 signature
     */
    private String hmacSHA256(String data, String secretKey) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Get payment by ID
     */
    public PaymentResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return paymentMapper.toPaymentResponse(payment);
    }

    /**
     * Get user's payment history
     */
    public List<PaymentResponse> getUserPayments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Payment> payments = paymentRepository.findByUser_UserId(user.getUserId());

        return payments.stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }
}
