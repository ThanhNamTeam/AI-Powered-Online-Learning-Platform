package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.entity.AISubscription;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.AISubscriptionRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "AI Subscription management")
public class SubscriptionController {

    private final AISubscriptionRepository aiSubscriptionRepository;
    private final UserRepository           userRepository;

    /**
     * GET /api/subscription
     * Trả về subscription hiện tại của user đang đăng nhập.
     * Nếu không có → trả về null data (success = true, data = null).
     */
    @GetMapping
    @Operation(
            summary  = "Get current user's active AI subscription",
            description = "Returns the active subscription for the logged-in instructor, or null if none.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<SubscriptionInfo>> getMySubscription() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success("No subscription", null));
        }

        List<AISubscription> valid = aiSubscriptionRepository.findValidSubscriptions(user);
        if (valid.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No active subscription", null));
        }

        AISubscription sub = valid.get(0);
        SubscriptionInfo info = SubscriptionInfo.builder()
                .subscriptionId(sub.getSubscriptionId().toString())
                .plan(sub.getPlan().name())
                .status(sub.getStatus().name())
                .active(sub.getStatus().name()) // satisfying frontend
                .aiCredits(sub.getAiCredits())
                .aiCreditsUsed(sub.getAiCreditsUsed())
                .remainingCredits(sub.getRemainingCredits())
                .startDate(sub.getStartDate() != null ? sub.getStartDate().toString() : null)
                .endDate(sub.getEndDate() != null ? sub.getEndDate().toString() : null)
                .expirationDate(sub.getEndDate() != null ? sub.getEndDate().toString() : null) // satisfying frontend
                .autoRenew(sub.getAutoRenew())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved", info));
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubscriptionInfo {
        private String  subscriptionId;
        private String  plan;
        private String  status;
        private String  active;         // For frontend compatibility
        private Integer aiCredits;
        private Integer aiCreditsUsed;
        private Integer remainingCredits;
        private String  startDate;
        private String  endDate;
        private String  expirationDate; // For frontend compatibility
        private Boolean autoRenew;
    }
}
