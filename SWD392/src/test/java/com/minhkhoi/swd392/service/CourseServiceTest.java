package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.request.CreateCourseRequest;
import com.minhkhoi.swd392.dto.response.CourseResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.mapper.CourseMapper;
import com.minhkhoi.swd392.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseMapper courseMapper;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private ModuleRepository moduleRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private ProgressRepository progressRepository;

    @InjectMocks
    private CourseService courseService;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

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
    void createCourse_Success() {
        // Arrange
        mockUserEmail(instructorEmail);
        User user = User.builder().email(instructorEmail).role(User.Role.INSTRUCTOR).build();
        when(userRepository.findByEmail(instructorEmail)).thenReturn(Optional.of(user));

        MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "test content".getBytes());
        CreateCourseRequest request = CreateCourseRequest.builder()
                .title("New Course")
                .thumbnailFile(thumbnailFile)
                .status(CourseStatus.DRAFT)
                .build();

        when(cloudinaryService.uploadFile(any(), eq("image"))).thenReturn(Map.of("secure_url", "http://cloudinary.com/test.jpg"));
        
        Course course = new Course();
        course.setTitle("New Course");
        when(courseMapper.toCourse(any(), any())).thenReturn(course);
        when(courseRepository.save(any())).thenReturn(course);
        when(courseMapper.toCourseResponse(any())).thenReturn(new CourseResponse());

        // Act
        CourseResponse result = courseService.createCourse(request);

        // Assert
        assertNotNull(result);
        verify(courseRepository).save(any());
        verify(cloudinaryService).uploadFile(any(), eq("image"));
    }

    @Test
    void createCourse_Forbidden_NotAnInstructor() {
        // Arrange
        mockUserEmail(instructorEmail);
        User user = User.builder().email(instructorEmail).role(User.Role.STUDENT).build();
        when(userRepository.findByEmail(instructorEmail)).thenReturn(Optional.of(user));

        CreateCourseRequest request = CreateCourseRequest.builder().build();

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> courseService.createCourse(request));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    void requestApproval_Success() {
        // Arrange
        mockUserEmail(instructorEmail);
        UUID courseId = UUID.randomUUID();
        User instructor = User.builder().email(instructorEmail).build();
        Course course = Course.builder()
                .courseId(courseId)
                .constructor(instructor)
                .status(CourseStatus.DRAFT)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(moduleRepository.countByCourse_CourseId(courseId)).thenReturn(1L);
        when(courseRepository.save(any())).thenReturn(course);
        when(courseMapper.toCourseResponse(any())).thenReturn(new CourseResponse());

        // Act
        CourseResponse result = courseService.requestApproval(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(CourseStatus.PENDING_APPROVAL, course.getStatus());
        verify(courseRepository).save(course);
    }

    @Test
    void requestApproval_NoModules_ThrowsException() {
        // Arrange
        mockUserEmail(instructorEmail);
        UUID courseId = UUID.randomUUID();
        User instructor = User.builder().email(instructorEmail).build();
        Course course = Course.builder()
                .courseId(courseId)
                .constructor(instructor)
                .status(CourseStatus.DRAFT)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(moduleRepository.countByCourse_CourseId(courseId)).thenReturn(0L);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> courseService.requestApproval(courseId));
        assertEquals(ErrorCode.MIN_MODULES_REQUIRED, exception.getErrorCode());
    }

    @Test
    void submitUpdateRequest_Success() {
        // Arrange
        mockUserEmail(instructorEmail);
        UUID courseId = UUID.randomUUID();
        User instructor = User.builder().email(instructorEmail).build();
        Course course = Course.builder()
                .courseId(courseId)
                .constructor(instructor)
                .status(CourseStatus.EDITING)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenReturn(course);
        when(courseMapper.toCourseResponse(any())).thenReturn(new CourseResponse());

        // Act
        CourseResponse result = courseService.submitUpdateRequest(courseId, "I added new lessons");

        // Assert
        assertNotNull(result);
        assertEquals(CourseStatus.PENDING_UPDATE, course.getStatus());
        assertEquals("I added new lessons", course.getPendingUpdateNote());
    }
}
