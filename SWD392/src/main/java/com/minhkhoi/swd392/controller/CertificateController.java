package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.constant.EnrollmentStatus;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.CertificateResponse;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificate", description = "APIs for viewing course completion certificates")
public class CertificateController {

    private final EnrollmentRepository enrollmentRepository;
    private final com.minhkhoi.swd392.repository.ProgressRepository progressRepository;

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "View Certificate", description = "Returns completion details if the student has finished the course.")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificate(@PathVariable UUID courseId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Enrollment enrollment = enrollmentRepository.findByUser_EmailAndCourse_CourseId(email, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Nếu status chưa là COMPLETED, kiểm tra xem thực tế đã hoàn thành hết bài học chưa
        if (enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            long totalLessons = progressRepository.countActiveLessonsByCourseId(courseId);
            long completedLessons = progressRepository.countCompletedByEnrollment(enrollment);
            
            log.info("Checking certificate for student {}: {}/{} lessons completed", 
                    email, completedLessons, totalLessons);

            if (totalLessons > 0 && completedLessons >= totalLessons) {
                // Tự động cập nhật thành COMPLETED nếu đã học xong 100%
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
                enrollment.setCompletedAt(java.time.LocalDateTime.now());
                enrollmentRepository.save(enrollment);
            } else if (totalLessons == 0) {
                // Trường hợp khóa học không có bài học nào, vẫn cho phép nếu đã đăng ký (tùy logic business)
                log.warn("Course {} has no lessons. Allowing certificate anyway.", courseId);
            } else {
                log.error("Certificate denied: Student {} only completed {}/{} lessons", 
                        email, completedLessons, totalLessons);
                throw new AppException(ErrorCode.COURSE_NOT_COMPLETED);
            }
        }

        CertificateResponse response = CertificateResponse.builder()
                .studentName(enrollment.getUser().getFullName())
                .courseTitle(enrollment.getCourse().getTitle())
                .completionDate(enrollment.getCompletedAt() != null ? enrollment.getCompletedAt() : java.time.LocalDateTime.now())
                .enrollmentId(enrollment.getEnrollmentId())
                .message("Chúc mừng! Bạn đã hoàn thành xuất sắc khóa học.")
                .build();

        return ResponseEntity.ok(ApiResponse.success("Certificate data retrieved", response));
    }
}
