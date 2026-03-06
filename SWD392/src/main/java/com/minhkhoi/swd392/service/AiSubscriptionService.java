package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.response.AiSubscriptionResponse;
import com.minhkhoi.swd392.entity.AISubscription;
import com.minhkhoi.swd392.repository.AISubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSubscriptionService {
    private final AISubscriptionRepository aiSubscriptionRepository;

    public AiSubscriptionResponse getPlanPresentStudent(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AISubscription subscription = aiSubscriptionRepository.findByInstructor_EmailAndStatus(email, AISubscription.SubscriptionStatus.ACTIVE);

        return AiSubscriptionResponse.builder()
                .plan(subscription.getPlan())
                .active(subscription.getStatus())
                .expirationDate(subscription.getEndDate())
                .build();
    }

}
