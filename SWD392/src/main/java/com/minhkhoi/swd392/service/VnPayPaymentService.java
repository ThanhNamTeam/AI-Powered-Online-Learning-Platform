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


        if ("COURSE".equals(planType)) {
            // ================= TRƯỜNG HỢP 1: MUA LẺ KHÓA HỌC =================

            // a. Validate Course ID
            if (request.getCourseId() == null) {
                throw new AppException(ErrorCode.COURSE_NOT_FOUND); // Hoặc lỗi "Course ID required"
            }

            Course targetCourse = courseRepository.findById(UUID.fromString(request.getCourseId()))
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

            // b. Tạo nội dung thanh toán
            orderInfo = "Pay for Course " + targetCourse.getTitle();
            paymentNote = "Course Purchase: " + targetCourse.getTitle();

            // c. Xử lý Enrollment (Ghi danh tạm hoặc lấy ghi danh cũ)
            // LƯU Ý: Nếu logic của bạn là chưa thanh toán thì chưa active,
            // bạn nên đảm bảo Enrollment này có trạng thái là PENDING hoặc chưa Active.
            enrollment = enrollmentRepository.findByUserAndCourse(user, targetCourse)
                    .orElseGet(() -> {
                        Enrollment newEnroll = Enrollment.builder()
                                .user(user)
                                .course(targetCourse)
                                .enrolledAt(LocalDateTime.now())
                                .status(EnrollmentStatus.PENDING) // Khuyên dùng: Thêm status cho enrollment
                                .build();
                        return enrollmentRepository.save(newEnroll);
                    });

        } else {
            // ================= TRƯỜNG HỢP 2: MUA GÓI SUBSCRIPTION =================

            // a. Validate Enum (BASIC, PREMIUM...)
            AISubscription.SubscriptionPlan plan;
            try {
                plan = AISubscription.SubscriptionPlan.valueOf(planType);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_PLAN);
            }

            // b. Tạo nội dung thanh toán
            orderInfo = "Pay for " + plan.name();
            paymentNote = "Subscription Plan: " + plan.name();

            // c. Logic Placeholder Course (Logic cũ của bạn)
            // (Tôi giữ nguyên logic này vì nó liên quan đến cách bạn quản lý Subscription)
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
                                .course(finalPlaceholderCourse) // Biến local variable phải final hoặc effectively final
                                .enrolledAt(LocalDateTime.now())
                                .build();
                        return enrollmentRepository.save(newEnroll);
                    });
        }

        // ---------------------------------------------------------
        Course course = courseRepository.findByCourseId(UUID.fromString(request.getCourseId()));

        // 3. Create Payment Record (PENDING)
        Payment payment = Payment.builder()
                .user(user)
                .enrollment(enrollment) // Link tới Enrollment thật hoặc Placeholder tùy case
                .amount(request.getAmount())
                .status(Payment.PaymentStatus.PENDING)
                .method(Payment.PaymentMethod.VNPAY)
                .transactionId(orderId)
                .notes(paymentNote)

                // QUAN TRỌNG: Lưu thêm type và courseId vào Payment để sau này IPN xử lý
                // Bạn cần thêm field này vào Entity Payment như đã bàn ở câu trước
                .type("COURSE".equals(planType) ? Payment.PaymentType.COURSE : Payment.PaymentType.SUBSCRIPTION)
                .course("COURSE".equals(planType) ? course : null)

                .build();

        payment = paymentRepository.save(payment);

        // 4. Build VNPAY URL
        // Lưu ý: orderInfo nên bỏ dấu tiếng Việt để tránh lỗi checksum VNPAY
        String safeOrderInfo = removeAccents(orderInfo);
        String vnp_Url = buildVnPayUrl(orderId, amount, safeOrderInfo, httpServletRequest);

        PaymentResponse response = paymentMapper.toPaymentResponse(payment);
        response.setPaymentUrl(vnp_Url);
        return response;
    }

    // Helper function để bỏ dấu tiếng Việt (nếu chưa có)
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

        // Remove trailing & if exists (safety)
        // logic above appends if itr.hasNext(), assuming last one is valid.

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.getPaymentUrl() + "?" + queryUrl;
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

        // Use filtered list for sorting and hashing
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
                // Build hash data
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

        if ("00".equals(responseCode)) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            createOrUpdateSubscription(payment);

            paymentRepository.save(payment);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    private String extractSubscriptionPlan(String notes) {
        if (notes.contains("BASIC")) return "BASIC";
        if (notes.contains("PREMIUM")) return "PREMIUM";
        if (notes.contains("ENTERPRISE")) return "ENTERPRISE";
        return "BASIC"; // Default
    }

    private void createOrUpdateSubscription(Payment payment) {
        User user = payment.getUser();

        // Extract subscription plan from payment notes
        String notes = payment.getNotes();
        String planName = extractSubscriptionPlan(notes);
        AISubscription.SubscriptionPlan plan = AISubscription.SubscriptionPlan.valueOf(planName);

        // Calculate subscription period (1 month)
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = null;
        if (plan.equals(AISubscription.SubscriptionPlan.BASIC)) {
            endDate = startDate.plusMonths(3);
        }

        if (plan.equals(AISubscription.SubscriptionPlan.PREMIUM)) {
            endDate = startDate.plusMonths(12);
        }

        if (plan.equals(AISubscription.SubscriptionPlan.ENTERPRISE)) {
            endDate = startDate.plusYears(2);
        }
        // Set AI credits based on plan
        Integer aiCredits = switch (plan) {
            case FREE -> 0;
            case BASIC -> 100;
            case PREMIUM -> 500;
            case ENTERPRISE -> null; // Unlimited
        };

        if (payment.getType().equals(Payment.PaymentType.SUBSCRIPTION)) {

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
        }
        log.info("Created subscription for user: {} with plan: {}", user.getEmail(), plan);
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
