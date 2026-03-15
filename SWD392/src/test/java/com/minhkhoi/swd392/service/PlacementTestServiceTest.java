package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.dto.placement.PlacementQuestionResponse;
import com.minhkhoi.swd392.dto.placement.PlacementTestResultResponse;
import com.minhkhoi.swd392.dto.placement.SubmitPlacementTestRequest;
import com.minhkhoi.swd392.dto.placement.SubmitPlacementTestRequest.PlacementAnswerItem;
import com.minhkhoi.swd392.entity.PlacementQuestion;
import com.minhkhoi.swd392.entity.PlacementQuestion.QuestionType;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.PlacementQuestionRepository;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlacementTestServiceTest {

    @Mock
    private PlacementQuestionRepository placementQuestionRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private GeminiService geminiService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlacementTestService placementTestService;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getRandomQuestions_Success() {
        // Arrange
        PlacementQuestion q1 = PlacementQuestion.builder()
                .id(UUID.randomUUID())
                .content("Q1")
                .questionType(QuestionType.READING)
                .build();
        PlacementQuestion q2 = PlacementQuestion.builder()
                .id(UUID.randomUUID())
                .content("Q2")
                .questionType(QuestionType.LISTENING)
                .build();

        when(placementQuestionRepository.findRandomByQuestionType(eq("LISTENING"), anyInt()))
                .thenReturn(List.of(q2));
        when(placementQuestionRepository.findRandomByQuestionType(eq("READING"), anyInt()))
                .thenReturn(List.of(q1));

        // Act
        List<PlacementQuestionResponse> result = placementTestService.getRandomQuestions(10);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 1);
        verify(placementQuestionRepository, atLeastOnce()).findRandomByQuestionType(anyString(), anyInt());
    }

    @Test
    void submitTest_GuestUser_Success() {
        // Arrange
        UUID qId = UUID.randomUUID();
        PlacementQuestion q = PlacementQuestion.builder()
                .id(qId)
                .content("Question?")
                .correctAnswer("A")
                .topic("Grammar")
                .jlptLevel(JlptLevel.N3)
                .build();

        PlacementAnswerItem answer = new PlacementAnswerItem();
        answer.setQuestionId(qId);
        answer.setSelectedAnswer("A");

        SubmitPlacementTestRequest request = new SubmitPlacementTestRequest();
        request.setAnswers(List.of(answer));

        when(placementQuestionRepository.findAllById(anyList())).thenReturn(List.of(q));
        when(geminiService.callGeminiWithPrompt(anyString())).thenReturn(
                "{\"estimatedLevel\": \"N3\", \"overallComment\": \"Good job\", \"strengths\": [\"Grammar\"], \"weaknesses\": [\"Kanji\"], \"studyRecommendation\": \"Keep it up\"}"
        );
        when(courseRepository.findByStatusAndJlptLevelIn(any(), anyList())).thenReturn(Collections.emptyList());

        // Mock anonymous authentication for guest
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false); // or true but "anonymousUser"

        // Act
        PlacementTestResultResponse result = placementTestService.submitTest(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCorrectCount());
        assertEquals(JlptLevel.N3, result.getEstimatedLevel());
        verify(userRepository, never()).save(any()); // Guests shouldn't be saved
    }
}
