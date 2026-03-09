package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.QuizResult;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {
    List<QuizResult> findByUserAndQuizOrderByCompletedAtDesc(User user, Quiz quiz);
    List<QuizResult> findByUser(User user);
}
