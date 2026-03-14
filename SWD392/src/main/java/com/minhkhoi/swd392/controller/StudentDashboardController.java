package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.StudentDashboardResponse;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.UserRepository;
import com.minhkhoi.swd392.service.StudentDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/dashboard")
@RequiredArgsConstructor
@Tag(name = "Student Dashboard", description = "APIs for student dashboard statistics")
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getDashboard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(ApiResponse.success("Fetch dashboard data successfully", 
                studentDashboardService.getDashboardData(user)));
    }
}
