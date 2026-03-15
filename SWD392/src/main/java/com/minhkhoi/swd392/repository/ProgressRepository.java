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


    Optional<Progress> findByEnrollmentAndLesson(Enrollment enrollment, Lesson lesson);


    long countByEnrollmentAndIsCompleted(Enrollment enrollment, Boolean isCompleted);


    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.courseId = :courseId")
    long countTotalLessonsByCourseId(@Param("courseId") java.util.UUID courseId);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.enrollment = :enrollment AND p.isCompleted = true")
    long countCompletedByEnrollment(@Param("enrollment") Enrollment enrollment);

    @Query("SELECT MAX(p.updatedAt) FROM Progress p WHERE p.enrollment = :enrollment")
    Optional<java.time.LocalDateTime> findMaxUpdatedAtByEnrollment(@Param("enrollment") Enrollment enrollment);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.enrollment.user.email = :email AND p.isCompleted = true")
    long countCompletedLessonsByUserEmail(@Param("email") String email);

    @Query("SELECT SUM(p.lesson.duration) FROM Progress p WHERE p.enrollment.user.email = :email AND p.isCompleted = true")
    Long sumStudyTimeByUserEmail(@Param("email") String email);
}
