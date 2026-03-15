package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.request.QuizSubmissionRequest;
import com.minhkhoi.swd392.dto.response.QuizSubmissionResponse;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.repository.QuizRepository;
import com.minhkhoi.swd392.repository.QuizResultRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private QuizResultRepository quizResultRepository;
    @Mock private UserRepository userRepository;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private QuizService quizService;

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
    void submitQuiz_WithCorrectAndIncorrectAnswers_Success() {
        // Arrange
        mockUserEmail();

        User student = User.builder().userId("user123").email(studentEmail).build();
        when(userRepository.findByEmail(studentEmail)).thenReturn(Optional.of(student));

        UUID quizId = UUID.randomUUID();
        UUID q1Id = UUID.randomUUID();
        UUID q2Id = UUID.randomUUID();

        Question q1 = Question.builder()
                .questionId(q1Id)
                .content("What is 1+1?")
                .correctAnswer("2")
                .explanation("Basic math")
                .build();
        Question q2 = Question.builder()
                .questionId(q2Id)
                .content("Capital of France?")
                .correctAnswer("Paris")
                .explanation("Geography knowledge")
                .build();

        Quiz quiz = Quiz.builder()
                .quizId(quizId)
                .title("AI Test")
                .questions(List.of(q1, q2))
                .build();
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        // Mock answers: answer 1 correct, answer 2 incorrect
        QuizSubmissionRequest.QuizAnswerItem a1 = new QuizSubmissionRequest.QuizAnswerItem(q1Id, "2");
        QuizSubmissionRequest.QuizAnswerItem a2 = new QuizSubmissionRequest.QuizAnswerItem(q2Id, "London");
        QuizSubmissionRequest request = new QuizSubmissionRequest(quizId, List.of(a1, a2));

        when(quizResultRepository.save(any(QuizResult.class))).thenReturn(new QuizResult());

        // Act
        QuizSubmissionResponse response = quizService.submitQuiz(request);

        // Assert
        assertNotNull(response);
        assertEquals(quizId, response.getQuizId());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(1, response.getCorrectAnswersCount());
        assertEquals(5.0, response.getScore()); // 1/2 * 10 = 5.0
        
        // Assert reviews match
        assertEquals(2, response.getQuestionDetails().size());
        assertTrue(response.getQuestionDetails().get(0).isCorrect());
        assertFalse(response.getQuestionDetails().get(1).isCorrect());
        assertEquals("Geography knowledge", response.getQuestionDetails().get(1).getExplanation());

        verify(quizResultRepository, times(1)).save(any(QuizResult.class));
    }

    @Test
    void submitQuiz_QuizNotFound_ThrowsException() {
        mockUserEmail();
        User student = User.builder().userId("user123").email(studentEmail).build();
        when(userRepository.findByEmail(studentEmail)).thenReturn(Optional.of(student));

        UUID quizId = UUID.randomUUID();
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        QuizSubmissionRequest request = new QuizSubmissionRequest(quizId, List.of());

        assertThrows(AppException.class, () -> quizService.submitQuiz(request));
        verify(quizResultRepository, never()).save(any());
    }
}
