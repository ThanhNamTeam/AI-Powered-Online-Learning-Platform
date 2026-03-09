package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.EnrollmentRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.EnrollmentResponse;
import com.minhkhoi.swd392.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> enrollCourse(@RequestBody EnrollmentRequest request) {
        try {
            enrollmentService.enrollCourse(request);
            return ResponseEntity.ok(ApiResponse.success("Enrolled in course successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getInstructorEnrollments() {
        try {
            var enrollments = enrollmentService.getEnrollmentsForInstructor();
            return ResponseEntity.ok(ApiResponse.success("Fetched instructor enrollments successfully", enrollments));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getStaffEnrollments() {
        try {
            var enrollments = enrollmentService.getAllEnrollments();
            return ResponseEntity.ok(ApiResponse.success("Fetched all enrollments successfully", enrollments));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
