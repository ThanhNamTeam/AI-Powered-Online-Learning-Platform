package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.QuizSubmissionRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.QuizSubmissionResponse;
import com.minhkhoi.swd392.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz Management", description = "APIs for student quiz submission and grading")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit Quiz (Student)", description = "Submit answers for a lesson quiz and receive grading and review.")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(@Valid @RequestBody QuizSubmissionRequest request) {
        log.info("Student is submitting quiz: {}", request.getQuizId());
        QuizSubmissionResponse response = quizService.submitQuiz(request);
        return ResponseEntity.ok(ApiResponse.success("Quiz submitted and graded successfully", response));
    }
}
