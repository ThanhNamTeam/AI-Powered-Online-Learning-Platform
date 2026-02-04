package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Course;
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

    boolean existsByUser_EmailAndEnrollment_CourseAndStatus(String email, Course enrollmentCourse, Payment.PaymentStatus status);

    Payment findByPaymentId(UUID paymentId);
}
