package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.config.VnPayConfig;
import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.constant.EnrollmentStatus;
import com.minhkhoi.swd392.dto.request.CreatePaymentRequest;
import com.minhkhoi.swd392.dto.response.PaymentResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.Payment;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.mapper.PaymentMapper;
import com.minhkhoi.swd392.repository.AISubscriptionRepository;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.PaymentRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VnPayPaymentServiceTest {

    @Mock private VnPayConfig vnPayConfig;
    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AISubscriptionRepository aiSubscriptionRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private PaymentMapper paymentMapper;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private VnPayPaymentService vnPayPaymentService;

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
    void createPayment_ForCourse_Success() {
        // Arrange
        mockUserEmail();

        User student = User.builder().userId("user123").email(studentEmail).role(User.Role.STUDENT).build();
        when(userRepository.findByEmail(studentEmail)).thenReturn(Optional.of(student));

        UUID courseId = UUID.randomUUID();
        Course course = Course.builder()
                .courseId(courseId)
                .title("Paid VNPay Course")
                .price(BigDecimal.valueOf(150000))
                .status(CourseStatus.APPROVED)
                .build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        when(enrollmentRepository.findByUserAndCourse(student, course)).thenReturn(Optional.empty());

        Enrollment enrollment = Enrollment.builder().status(EnrollmentStatus.PENDING).build();
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        // Mock VNPay Config
        when(vnPayConfig.getTmnCode()).thenReturn("VNPAY123");
        when(vnPayConfig.getHashSecret()).thenReturn("SECRET1234567890ABCDEF");
        when(vnPayConfig.getPaymentUrl()).thenReturn("http://sandbox.vnpayment.vn/paymentv2/vpcpay.html");

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setCourseId(courseId);
        request.setAmount(BigDecimal.valueOf(150000));

        Payment payment = Payment.builder().paymentId(UUID.randomUUID()).build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse mockResponse = new PaymentResponse();
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(mockResponse);

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader("X-FORWARDED-FOR")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        PaymentResponse result = vnPayPaymentService.createPayment(request, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertEquals("http://sandbox.vnpayment.vn/paymentv2/vpcpay.html", result.getPaymentUrl().split("\\?")[0]);
        assertTrue(result.getPaymentUrl().contains("vnp_Amount=15000000")); // Amount * 100 for VNPay
        
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
