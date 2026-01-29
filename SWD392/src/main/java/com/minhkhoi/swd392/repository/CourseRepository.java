package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByConstructor_UserId(String userId);
}
