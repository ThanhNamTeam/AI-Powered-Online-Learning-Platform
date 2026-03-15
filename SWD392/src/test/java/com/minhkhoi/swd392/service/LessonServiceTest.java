package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.request.CreateLessonRequest;
import com.minhkhoi.swd392.dto.response.LessonResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Lesson;
import com.minhkhoi.swd392.entity.Module;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.mapper.LessonMapper;
import com.minhkhoi.swd392.repository.LessonRepository;
import com.minhkhoi.swd392.repository.ModuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private LessonAsyncService lessonAsyncService;
    @Mock private LessonMapper lessonMapper;

    @InjectMocks
    private LessonService lessonService;

    @Test
    void createLesson_Success() {
        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(false);

            // Arrange
            UUID moduleId = UUID.randomUUID();
            Module module = Module.builder()
                    .moduleId(moduleId)
                    .course(Course.builder().status(CourseStatus.DRAFT).build())
                    .build();

            when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));

            MockMultipartFile videoFile = new MockMultipartFile("video", "test.mp4", "video/mp4", "video content".getBytes());
            CreateLessonRequest request = CreateLessonRequest.builder()
                    .moduleId(moduleId)
                    .title("Test Lesson")
                    .videoFile(videoFile)
                    .build();

            Lesson lesson = new Lesson();
            lesson.setLessonId(UUID.randomUUID());
            when(lessonMapper.toLesson(any(), any())).thenReturn(lesson);
            when(cloudinaryService.uploadVideo(any())).thenReturn(Map.of("secure_url", "http://cloudinary.com/test.mp4", "duration", 120));
            when(lessonRepository.save(any())).thenReturn(lesson);
            when(lessonMapper.toLessonResponse(any())).thenReturn(new LessonResponse());

            // Act
            LessonResponse result = lessonService.createLesson(request);

            // Assert
            assertNotNull(result);
            verify(lessonRepository).save(any());
            verify(lessonAsyncService).processTranscription(lesson.getLessonId());
        }
    }

    @Test
    void deleteLesson_Success_DraftMode() {
        // Arrange
        UUID lessonId = UUID.randomUUID();
        Module module = Module.builder()
                .course(Course.builder().status(CourseStatus.DRAFT).build())
                .build();
        Lesson lesson = Lesson.builder()
                .lessonId(lessonId)
                .module(module)
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // Act
        lessonService.deleteLesson(lessonId);

        // Assert
        verify(lessonRepository).delete(lesson);
    }

    @Test
    void deleteLesson_MarkPending_EditingMode() {
        // Arrange
        UUID lessonId = UUID.randomUUID();
        Module module = Module.builder()
                .course(Course.builder().status(CourseStatus.EDITING).build())
                .build();
        Lesson lesson = Lesson.builder()
                .lessonId(lessonId)
                .module(module)
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // Act
        lessonService.deleteLesson(lessonId);

        // Assert
        assertTrue(lesson.getIsPendingDeletion());
        verify(lessonRepository).save(lesson);
        verify(lessonRepository, never()).delete(any());
    }

    @Test
    void validateCourseMutable_ThrowsException_IfApproved() {
        // Arrange
        UUID lessonId = UUID.randomUUID();
        Module module = Module.builder()
                .course(Course.builder().status(CourseStatus.APPROVED).build())
                .build();
        Lesson lesson = Lesson.builder()
                .lessonId(lessonId)
                .module(module)
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> lessonService.deleteLesson(lessonId));
        assertEquals(ErrorCode.INVALID_COURSE_STATUS_FOR_UPDATE, exception.getErrorCode());
    }
}
