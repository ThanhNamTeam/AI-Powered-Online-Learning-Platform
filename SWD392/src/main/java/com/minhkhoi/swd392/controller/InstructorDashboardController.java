package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.InstructorDashboardResponse;
import com.minhkhoi.swd392.service.InstructorDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
@Tag(name = "Instructor Dashboard", description = "APIs for Instructor — requires INSTRUCTOR role")
public class InstructorDashboardController {

    private final InstructorDashboardService instructorDashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Instructor dashboard stats", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<InstructorDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("OK", instructorDashboardService.getDashboard()));
    }

    @GetMapping("/courses")
    @Operation(summary = "Instructor's own courses", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<InstructorDashboardService.CourseItem>>> getMyCourses(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success("OK", instructorDashboardService.getMyCourses(status)));
    }

    @GetMapping("/students")
    @Operation(summary = "Students enrolled in instructor's courses", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<InstructorDashboardService.StudentItem>>> getMyStudents() {
        return ResponseEntity.ok(ApiResponse.success("OK", instructorDashboardService.getMyStudents()));
    }
}
