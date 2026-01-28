package com.minhkhoi.swd392.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
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
     * Chỉ xử lý Transcribe video bất đồng bộ sau khi upload.
     */
    @Async
    @Transactional
    public void processTranscription(UUID lessonId) {
        log.info(">>>> Bắt đầu Transcription cho Lesson ID: {}", lessonId);
        try {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học ID: " + lessonId));

            if (lesson.getTranscript() != null && !lesson.getTranscript().isEmpty()) {
                log.info("Lesson {} đã có transcript.", lessonId);
                return;
            }

            String videoUrl = lesson.getVideoUrl();
            if (videoUrl == null || videoUrl.isEmpty()) {
                log.warn("Lesson {} không có videoUrl.", lessonId);
                return;
            }

            String transcript = assemblyAITranscriptionService.transcribeVideo(videoUrl, null);
            if (transcript != null) {
                lesson.setTranscript(transcript);
                lessonRepository.save(lesson);
                log.info("Đã lưu transcript cho Lesson {}", lessonId);
            }
        } catch (Exception e) {
            log.error("Lỗi transcribe Lesson {}: {}", lessonId, e.getMessage());
        }
    }

    /**
     * Tạo Quiz cho bài học (được gọi từ Controller khi Instructor nhấn nút).
     * Kiểm tra bản quyền Premium của Instructor.
     */
    public void validatePremiumInstructor(Lesson lesson) {
        User instructor = lesson.getModule().getCourse().getConstructor();
        List<AISubscription> validSubscriptions = aiSubscriptionRepository.findValidSubscriptions(instructor);

        boolean hasPremium = validSubscriptions.stream()
                .anyMatch(sub -> sub.getPlan() == AISubscription.SubscriptionPlan.PREMIUM);

        if (!hasPremium) {
            log.warn("Instructor {} không có gói PREMIUM. Không thể tạo Quiz.", instructor.getFullName());
            throw new AppException(ErrorCode.PREMIUM_REQUIRED);
        }
    }

    @Async
    @Transactional
    public void generateQuizByInstructor(UUID lessonId) {
        log.info(">>>> Instructor yêu cầu tạo Quiz cho Lesson ID: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        try {
            // 1. Chuyển trạng thái sang PROCESSING ngay lập tức để lock nút
            lesson.setQuizStatus(com.minhkhoi.swd392.constant.QuizStatus.PROCESSING);
            lesson.setLastQuizError(null);
            lessonRepository.saveAndFlush(lesson);

            // 2. Kiểm tra tài liệu đã sẵn sàng chưa (transcript)
            String transcript = lesson.getTranscript();
            String documentContent = lesson.getDocumentContent();

            if ((transcript == null || transcript.isEmpty()) && (documentContent == null || documentContent.isEmpty())) {
                throw new RuntimeException("Chưa có nội dung (video transcript hoặc tài liệu) để tạo Quiz.");
            }

            // 4. Mix nội dung và gọi Gemini
            StringBuilder combinedContent = new StringBuilder();
            if (transcript != null) combinedContent.append("Video Transcript:\n").append(transcript).append("\n\n");
            if (documentContent != null) combinedContent.append("Document Content:\n").append(documentContent);

            log.info("Đang gọi Gemini AI để tạo Quiz từ nội dung tổng hợp...");
            String rawJsonResponse = geminiService.generateQuizQuestions(combinedContent.toString());
            String cleanJson = cleanAiJsonResponse(rawJsonResponse);

            List<QuestionDTO> questionDTOs = objectMapper.readValue(cleanJson, new TypeReference<List<QuestionDTO>>() {});

            if (questionDTOs != null && !questionDTOs.isEmpty()) {
                Quiz quiz = Quiz.builder()
                        .lesson(lesson)
                        .title("AI Generated Quiz for: " + lesson.getTitle())
                        .build();
                quiz = quizRepository.save(quiz);

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
                
                // 5. Hoàn tất -> Chuyển sang COMPLETED
                lesson.setQuizStatus(com.minhkhoi.swd392.constant.QuizStatus.COMPLETED);
                lessonRepository.save(lesson);
                log.info("Hoàn tất tạo Quiz cho Lesson {}", lessonId);
            }
        } catch (Exception e) {
            log.error("Lỗi tạo Quiz cho Lesson {}: {}", lessonId, e.getMessage());
            // 6. Lỗi -> Chuyển sang FAILED và lưu message
            lesson.setQuizStatus(com.minhkhoi.swd392.constant.QuizStatus.FAILED);
            lesson.setLastQuizError(e.getMessage());
            lessonRepository.save(lesson);
        }
    }

    private String cleanAiJsonResponse(String raw) {
        if (raw == null) return "[]";
        return raw.replace("```json", "")
                .replace("```", "")
                .trim();
    }

    @Data
    public static class QuestionDTO {
        private String content;
        private Map<String, Object> options;
        private String correctAnswer;
        private String explanation;
    }
}