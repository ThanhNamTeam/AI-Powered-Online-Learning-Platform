package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    
    private UUID paymentId;
    private String userId;
    private String userEmail;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
    private Payment.PaymentMethod method;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String notes;
    private String paymentUrl; // For pending payments
}
