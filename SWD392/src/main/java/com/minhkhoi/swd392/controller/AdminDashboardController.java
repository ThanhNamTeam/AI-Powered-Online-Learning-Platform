package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.response.AdminDashboardResponse;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.CourseResponse;
import com.minhkhoi.swd392.dto.response.UserResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import com.minhkhoi.swd392.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "APIs for Admin — requires ADMIN role")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final UserRepository        userRepository;
    private final CourseRepository      courseRepository;
    private final EnrollmentRepository  enrollmentRepository;

    // ────────────────────────────────────────────────────────────────────────
    // GET /api/admin/dashboard  — tổng hợp stats + charts
    // ────────────────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    @Operation(summary = "Admin Dashboard Statistics",
               description = "Overview, user growth, revenue, course distribution, pending courses.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data retrieved", adminDashboardService.getDashboardStats())
        );
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET /api/admin/users  — Danh sách tất cả người dùng (kèm số khóa học)
    // ────────────────────────────────────────────────────────────────────────
    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin)",
               description = "Returns all users with enrollment count per user.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<AdminUserItem>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<AdminUserItem> result = users.stream()
                .filter(u -> u.getRole() != User.Role.ADMIN) // ẩn ADMIN account
                .map(u -> {
                    long courseCount = u.getRole() == User.Role.STUDENT
                            ? (u.getEnrollments() != null ? u.getEnrollments().size() : 0)
                            : (u.getCreatedCourses() != null ? u.getCreatedCourses().size() : 0);
                    return AdminUserItem.builder()
                            .userId(u.getUserId())
                            .fullName(u.getFullName())
                            .email(u.getEmail())
                            .role(u.getRole().name())
                            .enabled(Boolean.TRUE.equals(u.getEnabled()))
                            .imageUrl(u.getImageUrl())
                            .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null)
                            .courseCount((int) courseCount)
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", result));
    }

    // ────────────────────────────────────────────────────────────────────────
    // PUT /api/admin/users/{userId}/toggle-status  — bật/tắt tài khoản
    // ────────────────────────────────────────────────────────────────────────
    @PutMapping("/users/{userId}/toggle-status")
    @Operation(summary = "Toggle user account status",
               description = "Enable or disable a user account.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setEnabled(!Boolean.TRUE.equals(user.getEnabled()));
        userRepository.save(user);
        String msg = Boolean.TRUE.equals(user.getEnabled()) ? "kích hoạt" : "vô hiệu hóa";
        return ResponseEntity.ok(ApiResponse.success("Đã " + msg + " tài khoản thành công", null));
    }

    // ────────────────────────────────────────────────────────────────────────
    // DELETE /api/admin/users/{userId}  — xóa người dùng
    // ────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user (Admin)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa người dùng thành công", null));
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET /api/admin/courses  — Danh sách tất cả khóa học
    // ────────────────────────────────────────────────────────────────────────
    @GetMapping("/courses")
    @Operation(summary = "Get all courses (Admin)",
               description = "Returns all courses with status. Optional filter by status.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses(
            @RequestParam(value = "status", required = false) String status) {

        List<Course> courses;
        if (status != null && !status.isBlank()) {
            try {
                CourseStatus cs = CourseStatus.valueOf(status.toUpperCase());
                courses = courseRepository.findAll().stream()
                        .filter(c -> c.getStatus() == cs)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                courses = courseRepository.findAll();
            }
        } else {
            courses = courseRepository.findAll();
        }

        List<CourseResponse> result = courses.stream()
                .map(CourseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved", result));
    }

    // ────────────────────────────────────────────────────────────────────────
    // PUT /api/admin/courses/{courseId}/verify  — Duyệt / Từ chối khóa học
    // ────────────────────────────────────────────────────────────────────────
    @PutMapping("/courses/{courseId}/verify")
    @Operation(summary = "Approve or reject a course (Admin)",
               description = "Change course status to APPROVED or REJECTED.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseResponse>> verifyCourse(
            @PathVariable UUID courseId,
            @RequestParam String action,            // "APPROVED" | "REJECTED"
            @RequestParam(required = false) String reason) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        try {
            CourseStatus newStatus = CourseStatus.valueOf(action.toUpperCase());
            course.setStatus(newStatus);
            if (newStatus == CourseStatus.REJECTED && reason != null) {
                course.setRejectionReason(reason);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid action: " + action));
        }

        courseRepository.save(course);
        return ResponseEntity.ok(ApiResponse.success(
                "Course status updated to " + course.getStatus(),
                CourseResponse.fromEntity(course)
        ));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Inner DTO: AdminUserItem
    // ────────────────────────────────────────────────────────────────────────
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AdminUserItem {
        private String  userId;
        private String  fullName;
        private String  email;
        private String  role;
        private boolean enabled;
        private String  imageUrl;
        private String  createdAt;
        private int     courseCount;
    }
}
