package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.response.StaffDashboardResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Payment;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.PaymentRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StaffDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private StaffDashboardService staffDashboardService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getDashboardStats_Success() {
        // Arrange
        when(userRepository.countByRoleAndCreatedAtAfter(eq(User.Role.STUDENT), any(LocalDateTime.class)))
                .thenReturn(10L);

        when(paymentRepository.sumCompletedAmountAfter(any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1000000"));

        when(courseRepository.countByStatus(CourseStatus.PENDING_APPROVAL))
                .thenReturn(5L);

        // Mock Weekly Performance - Payment filter
        Payment mockPayment = Payment.builder()
                .amount(new BigDecimal("50000"))
                .status(Payment.PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
        when(paymentRepository.findByStatusAndCreatedAtAfter(eq(Payment.PaymentStatus.COMPLETED), any(LocalDateTime.class)))
                .thenReturn(List.of(mockPayment));

        // Mock Top courses
        Course course1 = Course.builder()
                .courseId(UUID.randomUUID())
                .title("Test Course A")
                .enrollments(List.of()) // 0 enrollments
                .build();
        Page<Course> topCoursesPage = new PageImpl<>(List.of(course1));
        
        when(courseRepository.findTopTrendingCourses(eq(""), any(Pageable.class)))
                .thenReturn(topCoursesPage);

        // Act
        StaffDashboardResponse response = staffDashboardService.getDashboardStats();

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getNewStudentsToday());
        assertEquals(new BigDecimal("1000000"), response.getRevenueToday());
        assertEquals(5L, response.getPendingRequests());
        assertEquals(4.8, response.getAverageRating());
        
        // Assert weekly performance
        assertNotNull(response.getWeeklyPerformance());
        assertEquals(7, response.getWeeklyPerformance().size()); // 7 days of the week

        // Assert top trending courses
        assertNotNull(response.getTopTrendingCourses());
        assertEquals(1, response.getTopTrendingCourses().size());
        assertEquals("Test Course A", response.getTopTrendingCourses().get(0).getTitle());
    }

    @Test
    void getDashboardStats_ReturnsZeroRevenue_IfNull() {
        // Arrange
        when(userRepository.countByRoleAndCreatedAtAfter(any(), any())).thenReturn(0L);
        when(paymentRepository.sumCompletedAmountAfter(any())).thenReturn(null);
        when(courseRepository.countByStatus(any())).thenReturn(0L);
        
        Page<Course> emptyPage = new PageImpl<>(List.of());
        when(courseRepository.findTopTrendingCourses(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        StaffDashboardResponse response = staffDashboardService.getDashboardStats();

        // Assert
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getRevenueToday());
    }
}
