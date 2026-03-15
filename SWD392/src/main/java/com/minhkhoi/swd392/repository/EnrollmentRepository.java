package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByUserAndCourse(User user, Course course);
    Optional<Enrollment> findByUserAndCourse(User user, Course course);
    List<Enrollment> findByCourseConstructor(User constructor);
    long countByCourse_Constructor_Email(String email);

    boolean existsByUser_EmailAndCourseAndStatusIn(String email, Course course,
            java.util.List<com.minhkhoi.swd392.constant.EnrollmentStatus> statuses);

    List<Enrollment> findByUser_UserId(String userId);

    Optional<Enrollment> findByUser_EmailAndCourse_CourseId(String email, UUID courseId);


}

