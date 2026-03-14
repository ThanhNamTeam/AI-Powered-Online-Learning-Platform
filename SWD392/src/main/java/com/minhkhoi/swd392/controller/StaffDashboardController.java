package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.StaffDashboardResponse;
import com.minhkhoi.swd392.service.StaffDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Dashboard", description = "APIs for Staff Dashboard")
public class StaffDashboardController {

    private final StaffDashboardService staffDashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Get Staff Dashboard Statistics", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StaffDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Staff dashboard data retrieved", staffDashboardService.getDashboardStats()));
    }
}
