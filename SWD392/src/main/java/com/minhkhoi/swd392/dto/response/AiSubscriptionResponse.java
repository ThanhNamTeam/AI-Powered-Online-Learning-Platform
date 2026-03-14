package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.entity.AISubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSubscriptionResponse {
    private AISubscription.SubscriptionPlan plan;
    private AISubscription.SubscriptionStatus active;
    private LocalDateTime expirationDate;
}
