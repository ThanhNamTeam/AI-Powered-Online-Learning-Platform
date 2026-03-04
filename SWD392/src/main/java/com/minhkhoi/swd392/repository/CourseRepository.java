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

    List<Course> findByConstructor_Email(String constructorEmail);

    /**
     * Tìm các khóa học đã được duyệt và phù hợp với mức JLPT.
     * Dùng để gợi ý khóa học sau khi đánh giá placement test.
     */
    List<Course> findByStatusAndJlptLevelIn(CourseStatus status, List<JlptLevel> levels);

    List<Course> findByStatusAndJlptLevel(CourseStatus status, JlptLevel jlptLevel);
}
