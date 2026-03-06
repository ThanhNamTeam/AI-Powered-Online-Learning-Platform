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

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
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

        enrollmentRepository.save(enrollment);
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
