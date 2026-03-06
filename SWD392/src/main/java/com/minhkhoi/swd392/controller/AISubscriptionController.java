package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.AiSubscriptionResponse;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.repository.AISubscriptionRepository;
import com.minhkhoi.swd392.service.AiSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class AISubscriptionController {
    private final AiSubscriptionService aiSubscriptionService;

    @GetMapping()
    public ResponseEntity<ApiResponse<AiSubscriptionResponse>> getSubscription() {
        AiSubscriptionResponse aiSubscriptionResponse = aiSubscriptionService.getPlanPresentStudent();
        return ResponseEntity.ok(ApiResponse.success("User info retrieved successfully", aiSubscriptionResponse));
    }
}
