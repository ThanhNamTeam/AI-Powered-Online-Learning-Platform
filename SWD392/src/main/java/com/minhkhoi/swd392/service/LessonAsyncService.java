package com.minhkhoi.swd392.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.repository.AISubscriptionRepository;
import com.minhkhoi.swd392.repository.LessonRepository;
import com.minhkhoi.swd392.repository.QuestionRepository;
import com.minhkhoi.swd392.repository.QuizRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonAsyncService {

    private final LessonRepository lessonRepository;
    private final AISubscriptionRepository aiSubscriptionRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AssemblyAITranscriptionService assemblyAITranscriptionService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    /**
     * Asynchronously process the video for a lesson.
     * Steps:
     * 1. Transcribe video.
     * 2. Check instructor subscription.
     * 3. Generate Quiz (if eligible).
     *
     * @param lessonId The ID of the lesson to process
     */
    @Async
    @Transactional
    public void processVideoForLesson(UUID lessonId) { 
        log.info("Starting background processing for Lesson ID: {}", lessonId);

        try {
            // Step 1 - Transcription
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));

            String videoUrl = lesson.getVideoUrl();
            if (videoUrl == null || videoUrl.isEmpty()) {
                log.warn("Lesson {} has no video URL. Stopping process.", lessonId);
                return;
            }

            // Call AssemblyAI to transcribe
            // Note: passing null for language code to let it auto-detect or use default
            String transcript = assemblyAITranscriptionService.transcribeVideo(videoUrl, null);
            
            // Validate transcript result
            if (transcript == null || transcript.isEmpty()) {
                log.error("Transcription failed or returned empty for Lesson {}", lessonId);
                return;
            }

            // Update transcript and save
            lesson.setTranscript(transcript);
            lessonRepository.save(lesson);
            log.info("Transcript saved for Lesson {}", lessonId);

            // Step 2 - Check Subscription (Business Logic)
            // Retrieve the instructor (User) via Course
            User instructor = lesson.getModule().getCourse().getConstructor();
            
            // Check for valid PREMIUM subscription
            // The requirement is: PREMIUM plan, ACTIVE status, endDate > Current Time
            List<AISubscription> validSubscriptions = aiSubscriptionRepository.findValidSubscriptions(instructor);
            
            boolean isPremiumUser = validSubscriptions.stream()
                    .anyMatch(sub -> sub.getPlan() == AISubscription.SubscriptionPlan.PREMIUM);

            if (!isPremiumUser) {
                log.info("User {} is not Premium or subscription is invalid. Skipping Quiz generation.", instructor.getUserId());
                return; // STOP the process
            }

            log.info("User {} is eligible. Proceeding to Quiz generation.", instructor.getUserId());

            // Step 3 - Generate Quiz (Only for Premium)
            String quizJson = geminiService.generateQuizQuestions(transcript);
            
            // Parse JSON
            List<QuestionDTO> questionDTOs = objectMapper.readValue(quizJson, new TypeReference<List<QuestionDTO>>() {});
            
            if (questionDTOs == null || questionDTOs.isEmpty()) {
                log.warn("Gemini returned no questions for Lesson {}", lessonId);
                return;
            }

            // Create and Save Quiz
            Quiz quiz = Quiz.builder()
                    .lesson(lesson)
                    .title("AI Generated Quiz for " + lesson.getTitle())
                    .build();
            quiz = quizRepository.save(quiz);

            // Save Questions
            for (QuestionDTO dto : questionDTOs) {
                Question question = Question.builder()
                        .quiz(quiz)
                        .content(dto.getContent())
                        .options(dto.getOptions())
                        .correctAnswer(dto.getCorrectAnswer())
                        .explanation(dto.getExplanation())
                        .build();
                questionRepository.save(question);
            }

            log.info("Successfully generated Quiz with {} questions for Lesson {}", questionDTOs.size(), lessonId);

        } catch (Exception e) {
            log.error("Error processing video for Lesson ID: {}", lessonId, e);
            // Optional: Update lesson status to ERROR if such a field existed
            // Since Lesson doesn't have an explicit status field in the entity provided, just logging the error.
        }
    }

    // DTO for parsing Gemini response
    @Data
    static class QuestionDTO {
        private String content;
        private Map<String, Object> options;
        private String correctAnswer;
        private String explanation;
    }
}
