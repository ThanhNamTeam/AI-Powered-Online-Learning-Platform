package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.PageResponse;
import com.minhkhoi.swd392.dto.request.CreateCourseRequest;
import com.minhkhoi.swd392.dto.request.VerifyCourseRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.CourseResponse;
import com.minhkhoi.swd392.dto.response.CourseStatsResponse;
import com.minhkhoi.swd392.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Course Management", description = "APIs for managing courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new course",
            description = "Create a new course (Only for INSTRUCTOR role). Status can be DRAFT or PENDING.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam("thumbnailFile") org.springframework.web.multipart.MultipartFile thumbnailFile,
            @RequestParam(value = "status", required = false) com.minhkhoi.swd392.constant.CourseStatus status) {
            
        CreateCourseRequest request = CreateCourseRequest.builder()
                .title(title)
                .description(description)
                .price(price)
                .thumbnailFile(thumbnailFile)
                .status(status)
                .build();

        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", response));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STAFF', 'INSTRUCTOR')")
    @Operation(summary = "Get all courses (Filtered by instructor ID if provided)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses(
            ) {
        return ResponseEntity.ok(ApiResponse.success("List courses", courseService.getAllCourses()));
    }


    @GetMapping("/all-course/students")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getAllCoursesForStudent(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("List courses for student", courseService.getAllCoursesForStudent(page, size)));
    }

    @GetMapping("/all-course/public")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getAllCoursesPublic(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sortBy", defaultValue = "trending") String sortBy
    ) {
        return ResponseEntity.ok(ApiResponse.success("List courses public", courseService.getAllCoursesPublic(page, size, search, sortBy)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<CourseStatsResponse>> getCourseStats() {
        return ResponseEntity.ok(ApiResponse.success("Course statistics", courseService.getCourseStats()));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success("List of instructor's courses", courseService.getCourseById(courseId)));
    }

    @PutMapping("/{courseId}/verify")
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Verify course (Approve/Reject) (For STAFF only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseResponse>> verifyCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody VerifyCourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Course verification processed", courseService.verifyCourse(courseId, request)));
    }
    @PostMapping("/{courseId}/submit-approval")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Submit Course for Approval (Instructor)", description = "Request approval for a DRAFT course. Must have at least 3 modules.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseResponse>> requestApproval(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success("Approval requested successfully", courseService.requestApproval(courseId)));
    }
}
