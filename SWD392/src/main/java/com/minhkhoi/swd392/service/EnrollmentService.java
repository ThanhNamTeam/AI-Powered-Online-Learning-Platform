package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.EnrollmentStatus;
import com.minhkhoi.swd392.dto.request.EnrollmentRequest;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minhkhoi.swd392.dto.response.EnrollmentResponse;
import java.util.List;
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

    @Transactional
    public void enrollCourse(EnrollmentRequest enrollmentRequest) {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        var course = courseRepository.findById(UUID.fromString(enrollmentRequest.getCourseId()))
                .orElseThrow(() -> new RuntimeException("Course not found: " + enrollmentRequest.getCourseId()));

        boolean alreadyEnrolled = enrollmentRepository.existsByUserAndCourse(user, course);
        if (alreadyEnrolled) {
            throw new RuntimeException("Bạn đã đăng ký khóa học này rồi.");
        }
        var enrollment = com.minhkhoi.swd392.entity.Enrollment.builder()
                .user(user)
                .course(course)
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
}