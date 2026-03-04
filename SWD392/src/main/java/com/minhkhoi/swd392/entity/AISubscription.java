package com.minhkhoi.swd392.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AISubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "subscription_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "subscription_start_date", nullable = false, updatable = false)
    private LocalDateTime startDate;

    @Column(name = "subscription_end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "subscription_auto_renew", nullable = false)
    @Builder.Default
    private Boolean autoRenew = false;

    @Column(name = "subscription_ai_credits")
    private Integer aiCredits;

    @Column(name = "subscription_ai_credits_used")
    @Builder.Default
    private Integer aiCreditsUsed = 0;

    @Column(name = "subscription_last_renewed_at")
    private LocalDateTime lastRenewedAt;

    @Column(name = "subscription_cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "subscription_notes", columnDefinition = "TEXT")
    private String notes;

    public enum SubscriptionPlan {
        BASIC,      // 100 AI credits/month
        PREMIUM,    // 500 AI credits/month
        ENTERPRISE  // Unlimited AI credits
    }

    public enum SubscriptionStatus {
        ACTIVE,
        EXPIRED,
        CANCELLED,
        SUSPENDED,
        PENDING
    }

    /**
     * Check if subscription is still valid
     */
    public boolean isValid() {
        return status == SubscriptionStatus.ACTIVE 
            && LocalDateTime.now().isBefore(endDate);
    }

    /**
     * Check if user has remaining AI credits
     */
    public boolean hasCreditsRemaining() {
        if (plan == SubscriptionPlan.ENTERPRISE) {
            return true; // Unlimited
        }
        return aiCredits != null && aiCreditsUsed < aiCredits;
    }

    /**
     * Get remaining AI credits
     */
    public Integer getRemainingCredits() {
        if (plan == SubscriptionPlan.ENTERPRISE) {
            return Integer.MAX_VALUE; // Unlimited
        }
        if (aiCredits == null) {
            return 0;
        }
        return Math.max(0, aiCredits - aiCreditsUsed);
    }
}
