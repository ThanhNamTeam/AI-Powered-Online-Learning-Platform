package com.minhkhoi.swd392.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<com.minhkhoi.swd392.entity.Module, UUID> {
    List<com.minhkhoi.swd392.entity.Module> findByCourse_CourseIdOrderByOrderIndexAsc(UUID courseId);
    
    long countByCourse_CourseId(UUID courseId);
}
