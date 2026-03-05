package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.config.VnPayConfig;
import com.minhkhoi.swd392.constant.EnrollmentStatus;
import com.minhkhoi.swd392.dto.request.CreatePaymentRequest;
import com.minhkhoi.swd392.dto.response.PaymentResponse;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.mapper.PaymentMapper;
import com.minhkhoi.swd392.repository.*;
import com.minhkhoi.swd392.constant.CourseStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayPaymentService {

    private final VnPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final PaymentMapper paymentMapper;
    private final AISubscriptionRepository aiSubscriptionRepository;
    private final EnrollmentService enrollmentService;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request, HttpServletRequest httpServletRequest) {
        // 1. Authenticate User
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Validate Role
        if (user.getRole() != User.Role.INSTRUCTOR && user.getRole() != User.Role.STUDENT) {
            throw new AppException(ErrorCode.ONLY_STUDENT_OR_INSTRUCTOR_CAN_PURCHASE);
        }

        String planType = request.getSubscriptionPlan().toUpperCase();
        String orderId = UUID.randomUUID().toString();
        long amount = request.getAmount().longValue() * 100;
        String orderInfo;
        Enrollment enrollment;
        String paymentNote;
        Payment.PaymentType paymentType;
        Course courseTarget;

        if ("COURSE".equals(planType)) {
            // ================= TRƯỜNG HỢP 1: MUA LẺ KHÓA HỌC =================
            paymentType = Payment.PaymentType.COURSE;

            if (request.getCourseId() == null) {
                throw new AppException(ErrorCode.COURSE_NOT_FOUND);
            }

            courseTarget = courseRepository.findById(UUID.fromString(request.getCourseId()))
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

            orderInfo = "Pay for Course " + courseTarget.getTitle();
            paymentNote = "Course Purchase: " + courseTarget.getTitle();

            enrollment = enrollmentRepository.findByUserAndCourse(user, courseTarget)
                    .orElseGet(() -> {
                        Enrollment newEnroll = Enrollment.builder()
                                .user(user)
                                .course(courseTarget) // Biến này cần final hoặc effectively final
                                .enrolledAt(LocalDateTime.now())
                                .status(EnrollmentStatus.PENDING)
                                .build();
                        return enrollmentRepository.save(newEnroll);
                    });

        } else {
            courseTarget = null;
            // ================= TRƯỜNG HỢP 2: MUA GÓI SUBSCRIPTION =================
            paymentType = Payment.PaymentType.SUBSCRIPTION;

            AISubscription.SubscriptionPlan plan;
            try {
                plan = AISubscription.SubscriptionPlan.valueOf(planType);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_PLAN);
            }

            orderInfo = "Pay for " + plan.name();
            paymentNote = "Subscription Plan: " + plan.name();

            // Placeholder Course Logic
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

            Course finalPlaceholderCourse = placeholderCourse;
            enrollment = enrollmentRepository.findByUserAndCourse(user, placeholderCourse)
                    .orElseGet(() -> {
                        Enrollment newEnroll = Enrollment.builder()
                                .user(user)
                                .course(finalPlaceholderCourse)
                                .enrolledAt(LocalDateTime.now())
                                .status(EnrollmentStatus.PENDING) // Thêm status cho đồng bộ
                                .build();
                        return enrollmentRepository.save(newEnroll);
                    });
        }

        // 3. Create Payment Record (PENDING)
        Payment payment = Payment.builder()
                .user(user)
                .enrollment(enrollment)
                .amount(request.getAmount())
                .status(Payment.PaymentStatus.PENDING)
                .method(Payment.PaymentMethod.VNPAY)
                .transactionId(orderId)
                .notes(paymentNote)
                .type(paymentType)
                .course(courseTarget) // Có thể null nếu là Subscription
                .build();

        payment = paymentRepository.save(payment);

        // 4. Build VNPAY URL
        String safeOrderInfo = removeAccents(orderInfo);
        String vnp_Url = buildVnPayUrl(orderId, amount, safeOrderInfo, httpServletRequest);

        PaymentResponse response = paymentMapper.toPaymentResponse(payment);
        response.setPaymentUrl(vnp_Url);
        return response;
    }

    @Transactional
    public void handleVnPayCallback(Map<String, String> params) {
        Map<String, String> vnp_Params = new HashMap<>(params);
        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");

        if (vnp_SecureHash == null) {
            log.error("VNPAY Callback missing SecureHash");
            throw new AppException(ErrorCode.INVALID_VNPAY_SIGNATURE);
        }

        vnp_Params.remove("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHashType");

        List<String> fieldNames = vnp_Params.keySet().stream()
                .filter(k -> vnp_Params.get(k) != null && !vnp_Params.get(k).isEmpty())
                .sorted()
                .collect(Collectors.toList());

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);

            try {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    hashData.append('&');
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        if (!calculatedHash.equals(vnp_SecureHash)) {
            log.error("Invalid VNPAY signature. Calculated: {}, Received: {}", calculatedHash, vnp_SecureHash);
            throw new AppException(ErrorCode.INVALID_VNPAY_SIGNATURE);
        }

        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        Payment payment = paymentRepository.findByTransactionId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        // Kiểm tra nếu thanh toán đã được xử lý rồi thì bỏ qua để tránh duplicate
        if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            return;
        }

        if ("00".equals(responseCode)) {
            // === THANH TOÁN THÀNH CÔNG ===
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            // Xử lý dựa trên loại thanh toán
            if (payment.getType() == Payment.PaymentType.COURSE) {
                // Case 1: Mua khóa học -> Active Enrollment
                Enrollment enrollment = payment.getEnrollment();
                if (enrollment != null) {
                    enrollment.setStatus(EnrollmentStatus.ACTIVE);
                    enrollment.setType(Enrollment.EnrollmentType.SINGLE_PURCHASE);
                    enrollmentRepository.save(enrollment);
                    log.info("Activated enrollment for course: {} user: {}", payment.getCourse().getTitle(), payment.getUser().getEmail());
                }
            } else if (payment.getType() == Payment.PaymentType.SUBSCRIPTION) {
                // Case 2: Mua gói -> Tạo Subscription
                createOrUpdateSubscription(payment);
            }

        } else {
            // === THANH TOÁN THẤT BẠI ===
            payment.setStatus(Payment.PaymentStatus.FAILED);
            log.warn("Payment failed for orderId: {}", orderId);
        }

        paymentRepository.save(payment);
    }

    private void createOrUpdateSubscription(Payment payment) {
        User user = payment.getUser();
        String email = user.getEmail();

        // 🔥 BƯỚC 1: Tắt subscription cũ
        AISubscription oldSub = aiSubscriptionRepository
                .findTopByInstructor_EmailAndStatusOrderByEndDateDesc(
                        email,
                        AISubscription.SubscriptionStatus.ACTIVE
                );

        if (oldSub != null) {
            oldSub.setStatus(AISubscription.SubscriptionStatus.CANCELLED);
            aiSubscriptionRepository.save(oldSub);
        }

        // 🔽 Phần code cũ của bạn giữ nguyên
        String notes = payment.getNotes();
        String planName = extractSubscriptionPlan(notes);
        AISubscription.SubscriptionPlan plan;

        try {
            plan = AISubscription.SubscriptionPlan.valueOf(planName);
        } catch (Exception e) {
            log.error("Unknown plan in payment notes: {}", planName);
            plan = AISubscription.SubscriptionPlan.BASIC;
        }

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate;
        Integer aiCredits;

        switch (plan) {
            case BASIC:
                endDate = startDate.plusMonths(3);
                aiCredits = 100;
                break;
            case PREMIUM:
                endDate = startDate.plusMonths(12);
                aiCredits = 500;
                break;
            case ENTERPRISE:
                endDate = startDate.plusYears(2);
                aiCredits = null;
                break;
            default:
                endDate = startDate.plusMonths(1);
                aiCredits = 0;
        }

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
                .notes("Activated via VnPay. Payment Ref: " + payment.getTransactionId())
                .build();

        aiSubscriptionRepository.save(subscription);

        log.info("Created subscription for user: {} with plan: {}", email, plan);
    }

    private String extractSubscriptionPlan(String notes) {
        if (notes == null) return "BASIC";
        String upperNotes = notes.toUpperCase();
        if (upperNotes.contains("PREMIUM")) return "PREMIUM";
        if (upperNotes.contains("ENTERPRISE")) return "ENTERPRISE";
        if (upperNotes.contains("BASIC")) return "BASIC";
        return "BASIC"; // Default
    }

    // ================= HELPER FUNCTIONS =================

    private String removeAccents(String s) {
        if (s == null) return "";
        String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return temp.replaceAll("\\p{M}", "").replaceAll("[^a-zA-Z0-9 ]", "");
    }

    private String buildVnPayUrl(String orderId, long amount, String orderInfo, HttpServletRequest request) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = orderId;
        String vnp_IpAddr = getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.getTmnCode();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(formatter));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.getPaymentUrl() + "?" + queryUrl;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    private String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}