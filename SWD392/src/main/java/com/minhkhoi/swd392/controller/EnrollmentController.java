package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.EnrollmentRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
