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
     * Xử lý video bất đồng bộ: Transcribe -> Check Subscription -> Generate Quiz.
     * @param lessonId ID của bài học cần xử lý.
     */
    @Async
    @Transactional
    public void processVideoForLesson(UUID lessonId) {
        log.info(">>>> Bắt đầu xử lý AI chạy ngầm cho Lesson ID: {}", lessonId);

        try {
            // 1. Lấy thông tin bài học
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học ID: " + lessonId));

            String transcript = lesson.getTranscript();

            if (transcript == null || transcript.trim().isEmpty()) {
                String videoUrl = lesson.getVideoUrl();
                if (videoUrl == null || videoUrl.isEmpty()) {
                    log.warn("Bài học {} không có link video. Dừng tiến trình.", lessonId);
                    return;
                }

                log.info("Đang gọi AssemblyAI để lấy nội dung video...");
                // Ngôn ngữ để null để AssemblyAI tự động nhận diện (Tiếng Nhật/Anh/Việt)
                transcript = assemblyAITranscriptionService.transcribeVideo(videoUrl, null);

                if (transcript != null && !transcript.isEmpty()) {
                    lesson.setTranscript(transcript);
                    lessonRepository.save(lesson);
                    log.info("Đã lưu transcript cho Lesson {}", lessonId);
                } else {
                    log.error("Không thể lấy được transcript cho Lesson {}", lessonId);
                    return;
                }
            } else {
                log.info("Lesson {} đã có sẵn transcript. Bỏ qua bước Transcribe.", lessonId);
            }

            // ----------------------------------------------------------------
            // BƯỚC 2: KIỂM TRA QUYỀN PREMIUM (Business Logic)
            // ----------------------------------------------------------------
            // Truy ngược từ Lesson -> Module -> Course -> Constructor (User)
            User instructor = lesson.getModule().getCourse().getConstructor();

            log.info("Đang kiểm tra gói hội viên cho Instructor: {}", instructor.getFullName());

            // Query kiểm tra gói ACTIVE, đúng User và chưa hết hạn
            List<AISubscription> validSubscriptions = aiSubscriptionRepository.findValidSubscriptions(instructor);

            boolean hasPremium = validSubscriptions.stream()
                    .anyMatch(sub -> sub.getPlan() == AISubscription.SubscriptionPlan.PREMIUM);

            if (!hasPremium) {
                log.info("Instructor {} không có gói PREMIUM. Kết thúc luồng sau khi lưu Transcript.", instructor.getUserId());
                return; // Chỉ lưu transcript, không tạo Quiz
            }

            // ----------------------------------------------------------------
            // BƯỚC 3: TẠO QUIZ BẰNG GEMINI AI
            // ----------------------------------------------------------------
            log.info("Đang gọi Gemini AI để tạo bộ câu hỏi trắc nghiệm...");
            String rawJsonResponse = geminiService.generateQuizQuestions(transcript);

            // Làm sạch chuỗi (loại bỏ markdown ```json ... ```)
            String cleanJson = cleanAiJsonResponse(rawJsonResponse);

            // Chuyển đổi JSON string thành List các DTO
            List<QuestionDTO> questionDTOs = objectMapper.readValue(cleanJson, new TypeReference<List<QuestionDTO>>() {});

            if (questionDTOs == null || questionDTOs.isEmpty()) {
                log.warn("Gemini không trả về câu hỏi nào cho bài học này.");
                return;
            }

            // ----------------------------------------------------------------
            // BƯỚC 4: LƯU TRỮ QUIZ VÀ CÂU HỎI
            // ----------------------------------------------------------------
            // Tạo tiêu đề Quiz dựa trên tên bài học
            Quiz quiz = Quiz.builder()
                    .lesson(lesson)
                    .title("AI Quiz: " + lesson.getTitle())
                    .build();
            quiz = quizRepository.save(quiz);

            for (QuestionDTO dto : questionDTOs) {
                Question question = Question.builder()
                        .quiz(quiz)
                        .content(dto.getContent())
                        .options(dto.getOptions()) // Map này sẽ được Hibernate lưu thành JSON trong DB
                        .correctAnswer(dto.getCorrectAnswer())
                        .explanation(dto.getExplanation())
                        .build();
                questionRepository.save(question);
            }

            log.info(">>>> HOÀN TẤT! Đã tạo thành công Quiz với {} câu hỏi cho Lesson {}", questionDTOs.size(), lessonId);

        } catch (Exception e) {
            log.error("LỖI trong quá trình xử lý Async cho Lesson {}: {}", lessonId, e.getMessage(), e);
        }
    }

    /**
     * Loại bỏ các ký tự Markdown JSON thường gặp khi AI trả về kết quả
     */
    private String cleanAiJsonResponse(String raw) {
        if (raw == null) return "[]";
        return raw.replace("```json", "")
                .replace("```", "")
                .trim();
    }

    /**
     * DTO nội bộ để ánh xạ dữ liệu từ Gemini JSON
     */
    @Data
    public static class QuestionDTO {
        private String content;
        private Map<String, Object> options;
        private String correctAnswer;
        private String explanation;
    }
}