package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.PageResponse;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.DiscussionResponse;
import com.minhkhoi.swd392.service.DiscussionService;
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
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@Tag(name = "Discussion Management", description = "APIs for managing Q&A and comments (Staff only)")
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "List discussions/Q&A with filter", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<DiscussionResponse>>> getDiscussions(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Discussions fetched", discussionService.getDiscussions(filter, search, page, size)));
    }

    @PostMapping("/{discussionId}/reply")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Reply to a discussion/question", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DiscussionResponse>> replyDiscussion(
            @PathVariable UUID discussionId,
            @RequestBody Map<String, String> body
    ) {
        String replyText = body.getOrDefault("reply", "");
        return ResponseEntity.ok(ApiResponse.success("Reply sent", discussionService.replyDiscussion(discussionId, replyText)));
    }

    @DeleteMapping("/{discussionId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Delete a discussion", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteDiscussion(@PathVariable UUID discussionId) {
        discussionService.deleteDiscussion(discussionId);
        return ResponseEntity.ok(ApiResponse.success("Discussion deleted", null));
    }
}
