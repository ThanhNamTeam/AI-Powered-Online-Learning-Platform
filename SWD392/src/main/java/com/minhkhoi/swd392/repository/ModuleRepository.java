package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {
    List<Module> findByCourse_CourseIdOrderByOrderIndexAsc(UUID courseId);
    
    long countByCourse_CourseId(UUID courseId);
}
