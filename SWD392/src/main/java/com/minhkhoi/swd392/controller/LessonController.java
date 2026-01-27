package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.QuestionResponse;
import com.minhkhoi.swd392.dto.response.QuizResponse;
import com.minhkhoi.swd392.entity.Quiz;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.QuizRepository;
import com.minhkhoi.swd392.service.LessonAsyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lesson Management", description = "APIs for lesson processing and quizzes")
public class LessonController {

    private final LessonAsyncService lessonAsyncService;
    private final QuizRepository quizRepository;

    @PostMapping("/{lessonId}/process-ai")
    @Operation(summary = "Process Video for Lesson",
            description = "Triggers async background task to transcribe video and generate quiz (Premium only)")
    public ResponseEntity<ApiResponse<Void>> processLessonVideo(@PathVariable UUID lessonId) {
        log.info("Received request to process AI for lesson ID: {}", lessonId);


        lessonAsyncService.processVideoForLesson(lessonId);


        return ResponseEntity.accepted()
                .body(ApiResponse.<Void>builder()
                        .message("AI processing started in background")
                        .build());
    }


    @GetMapping("/{lessonId}/quiz")
    @Operation(summary = "Get Generated Quiz", description = "Retrieve the latest AI-generated quiz for a specific lesson")
    public ResponseEntity<ApiResponse<QuizResponse>> getLessonQuiz(@PathVariable UUID lessonId) {

        Quiz quiz = quizRepository.findFirstByLesson_LessonIdOrderByCreatedAtDesc(lessonId)
                .orElse(null);


        if (quiz == null) {
            throw new AppException(ErrorCode.QUIZ_NOT_FOUND);
        }

        List<QuestionResponse> questions = quiz.getQuestions().stream()
                .map(q -> QuestionResponse.builder()
                        .questionId(q.getQuestionId())
                        .content(q.getContent())
                        .options(q.getOptions())
                        .correctAnswer(q.getCorrectAnswer())
                        .explanation(q.getExplanation())
                        .build())
                .collect(Collectors.toList());

        QuizResponse response = QuizResponse.builder()
                .quizId(quiz.getQuizId())
                .title(quiz.getTitle())
                .questions(questions)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Quiz retrieved successfully", response));
    }
}
