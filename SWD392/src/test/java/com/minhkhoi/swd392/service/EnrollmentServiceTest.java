package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.constant.EnrollmentStatus;
import com.minhkhoi.swd392.dto.request.EnrollmentRequest;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.PaymentRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentRepository paymentRepository;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private final String studentEmail = "student@test.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockUserEmail() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(studentEmail);
    }

    @Test
    void enrollCourse_Success() {
        // Arrange
        mockUserEmail();

        User student = User.builder().userId("user123").email(studentEmail).build();
        when(userRepository.findByEmail(studentEmail)).thenReturn(Optional.of(student));

        UUID courseId = UUID.randomUUID();
        Course course = Course.builder()
                .courseId(courseId)
                .title("Free Course")
                .price(BigDecimal.ZERO)
                .status(CourseStatus.APPROVED)
                .build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(false);

        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(UUID.randomUUID())
                .user(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentRequest request = EnrollmentRequest.builder()
                .courseId(courseId.toString())
                .type(Enrollment.EnrollmentType.SINGLE_PURCHASE)
                .build();

        // Act
        assertDoesNotThrow(() -> enrollmentService.enrollCourse(request));

        // Assert
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        verify(paymentRepository, never()).save(any());
    }
}
