package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.entity.AISubscription;
import com.minhkhoi.swd392.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponse {
    private Payment.PaymentStatus status;
    private Payment.PaymentType type;
    private BigDecimal amount;
    private String courseId;
    private String courseName;
    private AISubscription.SubscriptionPlan subscriptionPlan;

}
