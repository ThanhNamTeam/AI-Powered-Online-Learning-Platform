package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByUser_UserId(String userId);
    
    List<Payment> findByUser_UserIdAndStatus(String userId, Payment.PaymentStatus status);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt >= :date")
    java.math.BigDecimal sumCompletedAmountAfter(java.time.LocalDateTime date);

    List<Payment> findByStatusAndCreatedAtAfter(Payment.PaymentStatus status, java.time.LocalDateTime date);
}
