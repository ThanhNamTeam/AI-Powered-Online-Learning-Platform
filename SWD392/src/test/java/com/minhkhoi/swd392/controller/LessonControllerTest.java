package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.constant.QuizStatus;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Lesson;
import com.minhkhoi.swd392.entity.Module;
import com.minhkhoi.swd392.service.LessonService;
import com.minhkhoi.swd392.service.LessonAsyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LessonController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private LessonService lessonService;
    @MockBean private LessonAsyncService lessonAsyncService;
    @MockBean private com.minhkhoi.swd392.repository.QuizRepository quizRepository;
    @MockBean private com.minhkhoi.swd392.repository.LessonRepository lessonRepository;
    @MockBean private com.minhkhoi.swd392.repository.UserRepository userRepository;
    @MockBean private com.minhkhoi.swd392.mapper.QuizMapper quizMapper;
    @MockBean private com.minhkhoi.swd392.repository.ProgressRepository progressRepository;
    @MockBean private com.minhkhoi.swd392.repository.EnrollmentRepository enrollmentRepository;

    // Các mockBean security cơ bản
    @MockBean private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockBean private com.minhkhoi.swd392.service.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.minhkhoi.swd392.repository.RedisTokenRepository redisTokenRepository;

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void generateQuiz_Success() throws Exception {
        UUID lessonId = UUID.randomUUID();

        Course course = Course.builder().status(CourseStatus.APPROVED).build();
        Module module = Module.builder().course(course).build();
        Lesson lesson = Lesson.builder()
                .lessonId(lessonId)
                .module(module)
                // Default quizStatus is null or anything not COMPLETED/PROCESSING
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        doNothing().when(lessonAsyncService).validatePremiumInstructor(any(Lesson.class));
        doNothing().when(lessonAsyncService).generateQuizByInstructor(lessonId);

        mockMvc.perform(post("/api/lessons/{lessonId}/generate-quiz", lessonId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quiz generation in progress"));

        verify(lessonAsyncService, times(1)).validatePremiumInstructor(lesson);
        verify(lessonAsyncService, times(1)).generateQuizByInstructor(lessonId);
    }
}
