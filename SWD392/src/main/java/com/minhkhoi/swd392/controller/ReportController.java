package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.PageResponse;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.ReportResponse;
import com.minhkhoi.swd392.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report Management", description = "APIs for managing reports (Staff only)")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get all reports with filter/search", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<ReportResponse>>> getReports(
            @RequestParam(value = "status", defaultValue = "PENDING") String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Reports fetched", reportService.getReports(status, search, page, size)));
    }

    @PutMapping("/{reportId}/process")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Process a report (ignore, warn, delete)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReportResponse>> processReport(
            @PathVariable UUID reportId,
            @RequestBody Map<String, String> body
    ) {
        String action = body.getOrDefault("action", "IGNORE");
        String adminReply = body.get("adminReply");
        return ResponseEntity.ok(ApiResponse.success("Report processed", reportService.processReport(reportId, action, adminReply)));
    }
}
