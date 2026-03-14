package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // Ki\u1ec3m tra user \u0111\u00e3 c\u00f3 enrollment ACTIVE/COMPLETED cho kh\u00f3a h\u1ecdc ch\u01b0a
    boolean existsByUser_EmailAndCourseAndStatusIn(String email, Course course,
            java.util.List<com.minhkhoi.swd392.constant.EnrollmentStatus> statuses);

    List<Enrollment> findByUser_UserId(String userId);

    // Tìm enrollment theo email của user và courseId
    Optional<Enrollment> findByUser_EmailAndCourse_CourseId(String email, UUID courseId);

    // Tìm enrollment của user (theo email) cho 1 lesson cụ thể (qua course)
    Optional<Enrollment> findFirstByUser_EmailAndCourse_Modules_Lessons_LessonId(String email, UUID lessonId);
}
