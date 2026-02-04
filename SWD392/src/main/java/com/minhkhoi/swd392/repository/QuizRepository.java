package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    Optional<Quiz> findFirstByLesson_LessonIdOrderByCreatedAtDesc(UUID lessonId);
    List<Quiz> findByLesson_LessonIdOrderByCreatedAtDesc(UUID lessonId);
}
