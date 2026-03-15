package com.minhkhoi.swd392.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhkhoi.swd392.dto.request.QuizSubmissionRequest;
import com.minhkhoi.swd392.dto.response.QuizSubmissionResponse;
import com.minhkhoi.swd392.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizController.class)
@AutoConfigureMockMvc(addFilters = false)
public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    // Security mockBeans
    @MockBean private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockBean private com.minhkhoi.swd392.service.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.minhkhoi.swd392.repository.RedisTokenRepository redisTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "STUDENT")
    void submitQuiz_Success() throws Exception {
        UUID quizId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();

        QuizSubmissionRequest.QuizAnswerItem answerItem = new QuizSubmissionRequest.QuizAnswerItem(questionId, "A");
        QuizSubmissionRequest request = new QuizSubmissionRequest(quizId, List.of(answerItem));

        QuizSubmissionResponse.QuestionReviewDetail reviewDetail = QuizSubmissionResponse.QuestionReviewDetail.builder()
                .questionId(questionId)
                .questionContent("What is AI?")
                .yourAnswer("A")
                .correctAnswer("B")
                .explanation("AI is artificial intelligence. Please review the definition of AI.")
                .isCorrect(false)
                .build();

        QuizSubmissionResponse response = QuizSubmissionResponse.builder()
                .quizId(quizId)
                .quizTitle("AI Basics")
                .totalQuestions(1)
                .correctAnswersCount(0)
                .score(0.0)
                .questionDetails(List.of(reviewDetail))
                .build();

        when(quizService.submitQuiz(any(QuizSubmissionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/quizzes/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quizId").value(quizId.toString()))
                .andExpect(jsonPath("$.data.score").value(0.0))
                .andExpect(jsonPath("$.data.questionDetails[0].explanation").exists());
    }
}
