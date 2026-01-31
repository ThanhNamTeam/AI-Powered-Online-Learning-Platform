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

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

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

        enrollmentRepository.save(enrollment);
    }

}
