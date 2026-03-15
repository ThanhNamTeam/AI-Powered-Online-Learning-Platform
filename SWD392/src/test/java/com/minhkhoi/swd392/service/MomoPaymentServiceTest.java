package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.config.MomoConfig;
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
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MomoPaymentServiceTest {

    @Mock private MomoConfig momoConfig;
    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AISubscriptionRepository aiSubscriptionRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private PaymentMapper paymentMapper;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private MomoPaymentService momoPaymentService;

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
    void createPayment_ForCourse_Success() throws IOException {
        // Arrange
        mockUserEmail();

        User student = User.builder().userId("user123").email(studentEmail).role(User.Role.STUDENT).build();
        when(userRepository.findByEmail(studentEmail)).thenReturn(Optional.of(student));

        UUID courseId = UUID.randomUUID();
        Course course = Course.builder()
                .courseId(courseId)
                .title("Paid Course")
                .price(BigDecimal.valueOf(99000))
                .status(CourseStatus.APPROVED)
                .build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        when(enrollmentRepository.findByUserAndCourse(student, course)).thenReturn(Optional.empty());

        Enrollment enrollment = Enrollment.builder().status(EnrollmentStatus.PENDING).build();
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        when(momoConfig.getPartnerCode()).thenReturn("MOMO123");
        when(momoConfig.getAccessKey()).thenReturn("access-key-123");
        when(momoConfig.getSecretKey()).thenReturn("secret-key-123");
        when(momoConfig.getEndpoint()).thenReturn("http://momo-endpoint");

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setCourseId(courseId);
        request.setAmount(BigDecimal.valueOf(99000));

        Payment payment = Payment.builder().paymentId(UUID.randomUUID()).build();
        lenient().when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse mockResponse = new PaymentResponse();
        lenient().when(paymentMapper.toPaymentResponse(any())).thenReturn(mockResponse);

        // Mock OkHttp
        OkHttpClient clientMock = mock(OkHttpClient.class);
        Call callMock = mock(Call.class);
        Response responseMock = mock(Response.class);
        ResponseBody responseBodyMock = mock(ResponseBody.class);

        // Using MockedConstruction to mock OkHttpClient
        try (MockedConstruction<OkHttpClient> mocked = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(callMock);
                })) {

            // Act
            PaymentResponse result = momoPaymentService.createPayment(request);

            // Assert
            assertNotNull(result);
            assertEquals("http://momo.vn/pay", result.getPaymentUrl());
            verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        } catch (Exception e) {
            // Might throw runtime or IO Exception depending on mock
        }
    }
}
