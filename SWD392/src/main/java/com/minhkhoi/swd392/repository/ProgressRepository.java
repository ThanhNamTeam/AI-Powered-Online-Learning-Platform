package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.Lesson;
import com.minhkhoi.swd392.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, UUID> {

    // Tìm progress của 1 enrollment + 1 lesson cụ thể
    Optional<Progress> findByEnrollmentAndLesson(Enrollment enrollment, Lesson lesson);

    // Lấy tất cả progress của 1 enrollment
    List<Progress> findByEnrollment(Enrollment enrollment);

    // Đếm số lesson đã hoàn thành trong 1 enrollment
    long countByEnrollmentAndIsCompleted(Enrollment enrollment, Boolean isCompleted);

    // Đếm tổng số lesson trong khóa học (qua course→modules→lessons) bằng courseId
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.courseId = :courseId")
    long countTotalLessonsByCourseId(@Param("courseId") java.util.UUID courseId);

    // Đếm số lesson completed theo enrollment
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.enrollment = :enrollment AND p.isCompleted = true")
    long countCompletedByEnrollment(@Param("enrollment") Enrollment enrollment);
}
