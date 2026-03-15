package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.config.VnPayConfig;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.INSTRUCTOR && user.getRole() != User.Role.STUDENT) {
            throw new AppException(ErrorCode.ONLY_STUDENT_OR_INSTRUCTOR_CAN_PURCHASE);
        }

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

            Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                    .orElseGet(() -> Enrollment.builder()
                            .user(user)
                            .course(course)
                            .enrolledAt(LocalDateTime.now())
                            .status(com.minhkhoi.swd392.constant.EnrollmentStatus.PENDING)
                            .build());

            if (enrollment.getStatus() == com.minhkhoi.swd392.constant.EnrollmentStatus.ACTIVE
                    || enrollment.getStatus() == com.minhkhoi.swd392.constant.EnrollmentStatus.COMPLETED) {
                throw new RuntimeException("User already owns this course");
            }

            enrollment.setStatus(com.minhkhoi.swd392.constant.EnrollmentStatus.PENDING);
            enrollment = enrollmentRepository.save(enrollment);

            String orderId = UUID.randomUUID().toString();
            long amount = course.getPrice().longValue() * 100;
            String orderInfo = "Buy Course: " + course.getTitle();

            Payment payment = Payment.builder()
                    .user(user)
                    .enrollment(enrollment)
                    .amount(course.getPrice())
                    .status(Payment.PaymentStatus.PENDING)
                    .method(Payment.PaymentMethod.VNPAY)
                    .type(Payment.PaymentType.COURSE)
                    .course(course)
                    .transactionId(orderId)
                    .notes("Course: " + course.getTitle())
                    .build();
            payment = paymentRepository.save(payment);

            String vnp_Url = buildVnPayUrl(orderId, amount, orderInfo, httpServletRequest);
            PaymentResponse response = paymentMapper.toPaymentResponse(payment);
            response.setPaymentUrl(vnp_Url);
            return response;
        }

        AISubscription.SubscriptionPlan plan;
        try {
            plan = AISubscription.SubscriptionPlan.valueOf(request.getSubscriptionPlan().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_PLAN);
        }

        String orderId = UUID.randomUUID().toString();
        long amount = request.getAmount().longValue() * 100;
        String orderInfo = "Pay for " + plan.name();

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

        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, finalCourse)
                .orElseGet(() -> {
                    Enrollment newEnroll = Enrollment.builder()
                            .user(user)
                            .course(finalCourse)
                            .enrolledAt(LocalDateTime.now())
                            .build();
                    return enrollmentRepository.save(newEnroll);
                });

        Payment payment = Payment.builder()
                .user(user)
                .enrollment(enrollment)
                .amount(request.getAmount())
                .status(Payment.PaymentStatus.PENDING)
                .method(Payment.PaymentMethod.VNPAY)
                .type(Payment.PaymentType.SUBSCRIPTION)
                .course(finalCourse)
                .transactionId(orderId)
                .notes("Subscription Plan: " + plan.name())
                .build();
        payment = paymentRepository.save(payment);

        String vnp_Url = buildVnPayUrl(orderId, amount, orderInfo, httpServletRequest);

        PaymentResponse response = paymentMapper.toPaymentResponse(payment);
        response.setPaymentUrl(vnp_Url);
        return response;
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
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
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
            } catch(UnsupportedEncodingException e) {
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
            if (payment.getType() == Payment.PaymentType.COURSE) {
                Enrollment enrollment = payment.getEnrollment();
                if (enrollment != null) {
                    enrollment.setStatus(com.minhkhoi.swd392.constant.EnrollmentStatus.ACTIVE);
                    enrollmentRepository.save(enrollment);
                    log.info("VNPAY: Activated enrollment for user {} course {}",
                            payment.getUser().getEmail(), enrollment.getCourse().getTitle());
                }
            } else {
                createOrUpdateSubscription(payment);
            }

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
        if (notes.contains("FREE")) return "FREE";
        return "BASIC";
    }

    private void createOrUpdateSubscription(Payment payment) {
        User user = payment.getUser();
        String notes = payment.getNotes();
        String planName = extractSubscriptionPlan(notes);
        AISubscription.SubscriptionPlan plan = AISubscription.SubscriptionPlan.valueOf(planName);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(1);
        Integer aiCredits = switch (plan) {
            case BASIC -> 100;
            case PREMIUM -> 500;
            case ENTERPRISE -> null;
            case FREE -> 0;
        };
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