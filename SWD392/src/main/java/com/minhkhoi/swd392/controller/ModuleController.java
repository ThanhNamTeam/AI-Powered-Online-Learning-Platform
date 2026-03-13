package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.CreateModuleRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.ModuleResponse;
import com.minhkhoi.swd392.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Module Management", description = "APIs for creating and managing course modules")
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create Module (Instructor)", description = "Create a new module for a specific course.")
    public ResponseEntity<ApiResponse<ModuleResponse>> createModule(@RequestBody CreateModuleRequest request) {
        log.info("Instructor creating module: {}", request.getTitle());
        ModuleResponse response = moduleService.createModule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Module created successfully", response));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get Modules by Course", description = "Retrieve all modules associated with a specific course.")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> getModulesByCourse(@PathVariable UUID courseId) {
        List<ModuleResponse> response = moduleService.getModulesByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Modules retrieved successfully", response));
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Update Module", description = "Update an existing module's title or order.")
    public ResponseEntity<ApiResponse<ModuleResponse>> updateModule(
            @PathVariable UUID moduleId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer orderIndex) {
        log.info("Instructor updating module: {}", moduleId);
        ModuleResponse response = moduleService.updateModule(moduleId, title, orderIndex);
        return ResponseEntity.ok(ApiResponse.success("Module updated successfully", response));
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Delete Module", description = "Delete an existing module.")
    public ResponseEntity<ApiResponse<Void>> deleteModule(@PathVariable UUID moduleId) {
        log.info("Instructor deleting module: {}", moduleId);
        moduleService.deleteModule(moduleId);
        return ResponseEntity.ok(ApiResponse.success("Module deleted successfully", null));
    }
}
