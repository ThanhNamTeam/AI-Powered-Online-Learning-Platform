package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByUserAndCourse(User user,  Course course);
    java.util.Optional<Enrollment> findByUserAndCourse(User user, Course course);
    long countByUserAndType(User user, Enrollment.EnrollmentType type);

    @Query("""
    SELECT e.course.courseId
    FROM Enrollment e
    WHERE e.user.email = :email
      AND e.status IN ('ACTIVE', 'PENDING')
""")
    List<UUID> findCourseIdsByUserEmail(String email);
}