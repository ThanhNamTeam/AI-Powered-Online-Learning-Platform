package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.AISubscription;
import com.minhkhoi.swd392.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AISubscriptionRepository extends JpaRepository<AISubscription, UUID> {
    
    @Query("SELECT s FROM AISubscription s WHERE s.instructor = :instructor AND s.status = 'ACTIVE' AND s.endDate > CURRENT_TIMESTAMP")
    List<AISubscription> findValidSubscriptions(@Param("instructor") User instructor);

    AISubscription findByInstructor_EmailAndStatus(String instructorEmail, AISubscription.SubscriptionStatus status);

    AISubscription findTopByInstructor_EmailAndStatusOrderByEndDateDesc(String instructorEmail, AISubscription.SubscriptionStatus status);

}
