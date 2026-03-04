package com.minhkhoi.swd392.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = true)
    private Enrollment enrollment;

    @Column(name = "payment_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod method;

    @Column(name = "payment_transaction_id", unique = true)
    private String transactionId;

    @CreationTimestamp
    @Column(name = "payment_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_completed_at")
    private LocalDateTime completedAt;

    @Column(name = "payment_notes", columnDefinition = "TEXT")
    private String notes;

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED,
        CANCELLED
    }

    public enum PaymentMethod {
        VNPAY,
        MOMO
    }
}
