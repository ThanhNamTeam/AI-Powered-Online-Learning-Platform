package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
//    List<Course> findByConstructor_UserId(String userId);

    Page<Course> findByEnrollments_User_Email(String enrollmentsUserEmail, Pageable pageable);

    Page<Course> findByStatus(CourseStatus status,
                              Pageable pageable);

    Page<Course> findByStatusNot(CourseStatus status,
                              Pageable pageable);

    Page<Course> findByStatusAndTitleContainingIgnoreCase(CourseStatus status, String title, Pageable pageable);
    Page<Course> findByStatusAndJlptLevelAndTitleContainingIgnoreCase(CourseStatus status, JlptLevel jlptLevel, String title, Pageable pageable);

    Page<Course> findByStatusNotAndTitleContainingIgnoreCase(CourseStatus status, String title, Pageable pageable);

    long countByStatus(CourseStatus status);

    long countByStatusNot(CourseStatus status);

    List<Course> findByConstructor_Email(String constructorEmail);

    /**
     * Tìm các khóa học đã được duyệt và phù hợp với mức JLPT.
     * Dùng để gợi ý khóa học sau khi đánh giá placement test.
     */
    List<Course> findByStatusAndJlptLevelIn(CourseStatus status, List<JlptLevel> levels);

    List<Course> findByStatusAndJlptLevel(CourseStatus status, JlptLevel jlptLevel);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Course c WHERE c.status <> 'DRAFT' " +
            "ORDER BY CASE WHEN c.status = 'PENDING_APPROVAL' THEN 0 ELSE 1 END ASC, " +
            "c.createdAt ASC")
    Page<Course> findForStaff(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Course c WHERE c.status <> 'DRAFT' AND LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY CASE WHEN c.status = 'PENDING_APPROVAL' THEN 0 ELSE 1 END ASC, " +
            "c.createdAt ASC")
    Page<Course> findForStaffWithSearch(String search, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Course c LEFT JOIN Review r ON c.courseId = r.course.courseId " +
            "WHERE c.status = 'APPROVED' AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "GROUP BY c.courseId ORDER BY COALESCE(AVG(r.rating), 0) DESC, c.createdAt DESC")
    Page<Course> findTopRatedCourses(String search, Pageable pageable);

    Page<Course> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(CourseStatus status, String title, Pageable pageable);
    Page<Course> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtAsc(CourseStatus status, String title, Pageable pageable); 

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Course c LEFT JOIN c.enrollments e WHERE c.status = 'APPROVED' AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))) GROUP BY c.courseId ORDER BY COUNT(e) DESC")
    Page<Course> findTopTrendingCourses(String search, Pageable pageable);
}
