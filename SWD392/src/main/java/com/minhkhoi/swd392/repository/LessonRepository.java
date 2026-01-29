package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByModule_ModuleId(UUID moduleId);
}
