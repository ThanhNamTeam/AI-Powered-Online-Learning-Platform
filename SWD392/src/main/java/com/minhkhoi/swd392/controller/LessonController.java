package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.CreateLessonRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.LessonResponse;
import com.minhkhoi.swd392.dto.response.QuestionResponse;
import com.minhkhoi.swd392.dto.response.QuizResponse;
import com.minhkhoi.swd392.entity.Quiz;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.QuizRepository;
import com.minhkhoi.swd392.service.LessonAsyncService;
import com.minhkhoi.swd392.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lesson Management", description = "APIs for lesson processing and quizzes")
public class LessonController {

    private final LessonService lessonService;
    private final LessonAsyncService lessonAsyncService;
    private final QuizRepository quizRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create Lesson (Instructor)",
            description = "Upload video and optional document to create a lesson. Video transcription starts automatically.")
    public ResponseEntity<ApiResponse<LessonResponse>> uploadLesson(
            @RequestParam("title") String title,
            @RequestParam("moduleId") UUID moduleId,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestParam(value = "documentFile", required = false) MultipartFile documentFile) {
        
        log.info("Instructor uploading lesson: {}", title);
        
        CreateLessonRequest request = CreateLessonRequest.builder()
                .title(title)
                .moduleId(moduleId)
                .videoFile(videoFile)
                .documentFile(documentFile)
                .build();
        
        LessonResponse response = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lesson created and processing started", response));
    }

    @PostMapping("/{lessonId}/generate-quiz")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Generate Quiz (Instructor)",
            description = "Instructor clicks to generate a quiz based on video transcript and documents. Checks for Instructor's PREMIUM plan.")
    public ResponseEntity<ApiResponse<Void>> generateQuiz(@PathVariable UUID lessonId) {
        log.info("Instructor requested quiz generation for lesson: {}", lessonId);
        lessonAsyncService.generateQuizByInstructor(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Quiz generation in progress", null));
    }

    @GetMapping("/{lessonId}/quiz")
    @Operation(summary = "Get Generated Quiz", description = "Retrieve the latest AI-generated quiz for a specific lesson")
    public ResponseEntity<ApiResponse<QuizResponse>> getLessonQuiz(@PathVariable UUID lessonId) {
        Quiz quiz = quizRepository.findFirstByLesson_LessonIdOrderByCreatedAtDesc(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

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
