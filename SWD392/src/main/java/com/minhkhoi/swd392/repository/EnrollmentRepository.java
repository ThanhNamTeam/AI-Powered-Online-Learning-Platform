package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByUserAndCourse(User user, Course course);
    java.util.Optional<Enrollment> findByUserAndCourse(User user, Course course);
    List<Enrollment> findByCourseConstructor(User constructor);

    // Ki\u1ec3m tra user \u0111\u00e3 c\u00f3 enrollment ACTIVE/COMPLETED cho kh\u00f3a h\u1ecdc ch\u01b0a
    boolean existsByUser_EmailAndCourseAndStatusIn(String email, Course course,
            java.util.List<com.minhkhoi.swd392.constant.EnrollmentStatus> statuses);

    List<Enrollment> findByUser_UserId(String userId);
}
