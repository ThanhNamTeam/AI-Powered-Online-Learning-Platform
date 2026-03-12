package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.constant.QuizStatus;
import com.minhkhoi.swd392.dto.request.CreateLessonRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.LessonResponse;
import com.minhkhoi.swd392.dto.response.QuestionResponse;
import com.minhkhoi.swd392.dto.response.QuizResponse;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.Lesson;
import com.minhkhoi.swd392.entity.Progress;
import com.minhkhoi.swd392.entity.Quiz;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.LessonRepository;
import com.minhkhoi.swd392.repository.ProgressRepository;
import com.minhkhoi.swd392.repository.QuizRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import com.minhkhoi.swd392.entity.User;
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
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final com.minhkhoi.swd392.mapper.QuizMapper quizMapper;
    private final ProgressRepository progressRepository;
    private final EnrollmentRepository enrollmentRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create Lesson (Instructor)",
            description = "Upload video and optional document to create a lesson. Video transcription starts automatically.")
    public ResponseEntity<ApiResponse<LessonResponse>> uploadLesson(
            @RequestParam("title") String title,
            @RequestParam("moduleId") UUID moduleId,
            @RequestParam(value = "videoFile") MultipartFile videoFile,
            @RequestParam(value = "documentFile", required = false) MultipartFile documentFile) {
        

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

    @GetMapping("/module/{moduleId}")
    @Operation(summary = "Get Lessons by Module", description = "Retrieve all lessons associated with a specific module")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getLessonsByModule(@PathVariable UUID moduleId) {
        return ResponseEntity.ok(ApiResponse.success("Lessons retrieved successfully", 
                lessonService.getLessonsByModule(moduleId)));
    }

    @GetMapping("/{lessonId}/document")
    @Operation(summary = "Download Lesson Document", description = "Redirect to the downloadable URL for the lesson document (PDF)")
    public ResponseEntity<Void> downloadDocument(@PathVariable UUID lessonId) {
        String documentUrl = lessonService.getDownloadDocumentUrl(lessonId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(java.net.URI.create(documentUrl))
                .build();
    }

    @PostMapping("/{lessonId}/generate-quiz")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Generate Quiz (Instructor)",
            description = "Instructor clicks to generate a quiz based on video transcript and documents. Checks for Instructor's PREMIUM plan.")
    public ResponseEntity<ApiResponse<Void>> generateQuiz(@PathVariable UUID lessonId) {
        log.info("Instructor requested quiz generation for lesson: {}", lessonId);
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getModule().getCourse().getStatus() != com.minhkhoi.swd392.constant.CourseStatus.APPROVED) {
            throw new AppException(ErrorCode.COURSE_NOT_APPROVED);
        }

        if (lesson.getQuizStatus() == QuizStatus.COMPLETED) {
            throw new AppException(ErrorCode.QUIZ_ALREADY_EXISTS);
        }

        if (lesson.getQuizStatus() == QuizStatus.PROCESSING) {
            throw new AppException(ErrorCode.QUIZ_GENERATION_IN_PROGRESS);
        }

        // Kiểm tra Premium ĐỒNG BỘ trước khi chạy Async
        lessonAsyncService.validatePremiumInstructor(lesson);

        lessonAsyncService.generateQuizByInstructor(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Quiz generation in progress", null));
    }

    @GetMapping("/{lessonId}/quiz")
    @Operation(summary = "Get Latest Quiz", description = "Retrieve the latest AI-generated quiz for a specific lesson")
    public ResponseEntity<ApiResponse<QuizResponse>> getLessonQuiz(@PathVariable UUID lessonId) {
        Quiz quiz = quizRepository.findFirstByLesson_LessonIdOrderByCreatedAtDesc(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        QuizResponse response = quizMapper.toQuizResponse(quiz);

        // Hide answers for students so they can't cheat
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser != null && currentUser.getRole() == User.Role.STUDENT) {
            response.hideAnswers();
        }

        return ResponseEntity.ok(ApiResponse.success("Quiz retrieved successfully", response));
    }

    @GetMapping("/{lessonId}/quizzes")
    @Operation(summary = "Get All Quizzes", description = "Retrieve all quizzes for a specific lesson (ordered by created date)")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getAllLessonQuizzes(@PathVariable UUID lessonId) {
        // Verify lesson exists
        lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        List<Quiz> quizzes = quizRepository.findByLesson_LessonIdOrderByCreatedAtDesc(lessonId);
        
        List<QuizResponse> responses = quizzes.stream()
                .map(quizMapper::toQuizResponse)
                .collect(Collectors.toList());

        // Hide answers for students
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser != null && currentUser.getRole() == User.Role.STUDENT) {
            responses.forEach(QuizResponse::hideAnswers);
        }

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Found %d quiz(es) for lesson", responses.size()), 
                responses));
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Delete Lesson (Instructor)",
            description = "Delete a lesson, its video/document from Cloudinary, and its transcript from database")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable UUID lessonId) {
        log.info("Instructor requested lesson deletion: {}", lessonId);
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson deleted successfully", null));
    }

    @DeleteMapping("/{lessonId}/video")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Delete Video from Lesson (Instructor)",
            description = "Remove video from a lesson, delete from Cloudinary, and clear transcript in database")
    public ResponseEntity<ApiResponse<Void>> deleteLessonVideo(@PathVariable UUID lessonId) {
        log.info("Instructor requested video removal from lesson: {}", lessonId);
        lessonService.deleteVideoFromLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Video and transcript removed successfully", null));
    }

    /**
     * ✅ Student đánh dấu bài học là đã hoàn thành
     * Tạo/cập nhật bản ghi Progress trong DB
     */
    @PostMapping("/{lessonId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Mark Lesson as Completed (Student)",
            description = "Student marks a lesson as watched/completed. Creates or updates a Progress record.")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> completeLesson(
            @PathVariable UUID lessonId) {

        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        // Tìm lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        // Tìm enrollment của student trong khóa học này
        UUID courseId = lesson.getModule().getCourse().getCourseId();
        Enrollment enrollment = enrollmentRepository
                .findByUser_EmailAndCourse_CourseId(email, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Tạo hoặc cập nhật Progress
        Progress progress = progressRepository
                .findByEnrollmentAndLesson(enrollment, lesson)
                .orElse(Progress.builder()
                        .enrollment(enrollment)
                        .lesson(lesson)
                        .build());

        progress.setIsCompleted(true);
        progress.setLastWatchedTime(0);
        progressRepository.save(progress);

        // Tính % hoàn thành
        long totalLessons = enrollment.getCourse().getModules().stream()
                .flatMap(m -> m.getLessons().stream())
                .count();
        long completedLessons = progressRepository.countByEnrollmentAndIsCompleted(enrollment, true);
        int progressPercent = totalLessons > 0 ? (int)(completedLessons * 100 / totalLessons) : 0;

        log.info("Student {} completed lesson {} | Progress: {}/{} = {}%",
                email, lessonId, completedLessons, totalLessons, progressPercent);

        return ResponseEntity.ok(ApiResponse.success("Lesson marked as completed",
                java.util.Map.of(
                        "lessonId", lessonId,
                        "completedLessons", completedLessons,
                        "totalLessons", totalLessons,
                        "progressPercent", progressPercent
                )));
    }
}
