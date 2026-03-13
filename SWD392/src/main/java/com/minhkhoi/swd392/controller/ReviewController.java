package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.ReviewRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.UserRepository;
import com.minhkhoi.swd392.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "APIs for course reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> submitReview(@Valid @RequestBody ReviewRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        reviewService.submitReview(user, request);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá của bạn đã được gửi thành công", null));
    }
}
