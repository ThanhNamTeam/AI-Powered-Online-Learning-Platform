package com.minhkhoi.swd392.service;

import ch.qos.logback.core.joran.spi.ElementSelector;
import com.minhkhoi.swd392.constant.EnrollmentStatus;
import com.minhkhoi.swd392.dto.request.EnrollmentRequest;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.minhkhoi.swd392.dto.response.EnrollmentResponse;
import java.util.List;

import java.time.LocalDateTime;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    private final com.minhkhoi.swd392.repository.PaymentRepository paymentRepository;

    private final AISubscriptionRepository aiSubscriptionRepository;
    private final PaymentRepository paymentRepository;


    @Transactional
    public void enrollCourse(EnrollmentRequest enrollmentRequest) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Course course = courseRepository.findById(UUID.fromString(enrollmentRequest.getCourseId()))
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new AppException(ErrorCode.ALREADY_ENROLLED);
        }

        if (enrollmentRequest.getType() == Enrollment.EnrollmentType.SUBSCRIPTION) {

            AISubscription subscription =
                    aiSubscriptionRepository.findByInstructor_EmailAndStatus(email, AISubscription.SubscriptionStatus.ACTIVE);

            if (subscription == null) {
                throw new AppException(ErrorCode.SUBSCRIPTION_REQUIRED);
            }

            if (subscription.getEndDate() != null &&
                    subscription.getEndDate().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.SUBSCRIPTION_EXPIRED);
            }

            if (subscription.getPlan() == AISubscription.SubscriptionPlan.FREE) {
                throw new AppException(ErrorCode.FREE_NOT_ALLOW_ASSIGN);
            }

            long enrollCount = enrollmentRepository
                    .countByUserAndType(user, Enrollment.EnrollmentType.SUBSCRIPTION);

            int maxCoursesByPlan = getMaxCoursesByPlan(subscription.getPlan());

            if (enrollCount >= maxCoursesByPlan) {
                throw new AppException(ErrorCode.PLAN_LIMIT_REACHED);
            }

        } else if (enrollmentRequest.getType() == Enrollment.EnrollmentType.SINGLE_PURCHASE) {

            boolean paid = paymentRepository
                    .existsByUser_EmailAndEnrollment_CourseAndStatus(
                            user.getEmail(), course, Payment.PaymentStatus.COMPLETED);

            if (!paid) {
                throw new AppException(ErrorCode.PAYMENT_REQUIRED);
            }
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .type(enrollmentRequest.getType())
                .status(EnrollmentStatus.ACTIVE)
                .build();

        enrollment = enrollmentRepository.save(enrollment);

        // Add mock payment to simulate revenue for the instructor
        if (course.getPrice() != null && course.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            var payment = com.minhkhoi.swd392.entity.Payment.builder()
                    .user(user)
                    .enrollment(enrollment)
                    .amount(course.getPrice())
                    .status(com.minhkhoi.swd392.entity.Payment.PaymentStatus.COMPLETED)
                    .method(com.minhkhoi.swd392.entity.Payment.PaymentMethod.VNPAY) // Mocking as VNPAY
                    .transactionId("MOCK-" + UUID.randomUUID().toString())
                    .completedAt(java.time.LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);
        }
    }

    public List<EnrollmentResponse> getEnrollmentsForInstructor() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        var instructor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Instructor not found: " + email));

        return enrollmentRepository.findByCourseConstructor(instructor).stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());
    }


    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());
    }


    public int getMaxCoursesByPlan(AISubscription.SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> 0;
            case BASIC -> 5;
            case PREMIUM -> 50;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }

}
