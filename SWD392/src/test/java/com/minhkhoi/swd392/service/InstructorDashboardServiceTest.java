package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.response.InstructorDashboardResponse;
import com.minhkhoi.swd392.entity.*;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorDashboardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private InstructorDashboardService instructorDashboardService;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private final String instructorEmail = "instructor@test.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockUserEmail(String email) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
    }

    @Test
    void getMyCourses_Success() {
        // Arrange
        mockUserEmail(instructorEmail);
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder()
                .courseId(courseId)
                .title("Test Course")
                .status(CourseStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .price(BigDecimal.valueOf(100))
                .build();

        when(courseRepository.findByConstructor_Email(instructorEmail)).thenReturn(List.of(course));
        
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));

        // Act
        List<InstructorDashboardService.CourseItem> result = instructorDashboardService.getMyCourses(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        assertEquals(1, result.get(0).getEnrollmentCount());
        verify(courseRepository).findByConstructor_Email(instructorEmail);
    }

    @Test
    void getMyStudents_Success() {
        // Arrange
        mockUserEmail(instructorEmail);
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder()
                .courseId(courseId)
                .title("Test Course")
                .build();

        User student = User.builder()
                .userId("student1")
                .fullName("John Doe")
                .email("student@test.com")
                .build();

        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(UUID.randomUUID())
                .course(course)
                .user(student)
                .enrolledAt(LocalDateTime.now())
                .build();

        when(courseRepository.findByConstructor_Email(instructorEmail)).thenReturn(List.of(course));
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));

        // Act
        List<InstructorDashboardService.StudentItem> result = instructorDashboardService.getMyStudents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
        assertEquals(1, result.get(0).getEnrolledCourseCount());
    }

    @Test
    void getDashboard_Success() {
        // Arrange
        mockUserEmail(instructorEmail);
        User instructor = User.builder().email(instructorEmail).build();
        when(userRepository.findByEmail(instructorEmail)).thenReturn(Optional.of(instructor));

        UUID courseId = UUID.randomUUID();
        Course course = Course.builder()
                .courseId(courseId)
                .title("Test Course")
                .status(CourseStatus.APPROVED)
                .createdAt(LocalDateTime.now().minusMonths(1))
                .price(BigDecimal.valueOf(100))
                .build();

        when(courseRepository.findByConstructor_Email(instructorEmail)).thenReturn(List.of(course));

        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(UUID.randomUUID())
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .build();
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .enrollment(enrollment)
                .amount(BigDecimal.valueOf(100))
                .status(Payment.PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        // Act
        InstructorDashboardResponse result = instructorDashboardService.getDashboard();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalCourses());
        assertEquals(1, result.getApprovedCourses());
        assertEquals(1, result.getTotalEnrollments());
        assertEquals(0, result.getTotalRevenue().compareTo(BigDecimal.valueOf(100)));
        assertEquals(6, result.getRevenueByMonth().size());
        assertEquals(6, result.getEnrollmentByMonth().size());
    }

    @Test
    void getDashboard_InstructorNotFound_ThrowsException() {
        // Arrange
        mockUserEmail(instructorEmail);
        when(userRepository.findByEmail(instructorEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> instructorDashboardService.getDashboard());
    }
}
