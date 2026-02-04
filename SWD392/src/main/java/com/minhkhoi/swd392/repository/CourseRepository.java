package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.constant.CourseStatus;
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

    Page<Course> findByEnrollments_User_EmailAndStatus(String enrollmentsUserEmail, CourseStatus status, Pageable pageable);

    Page<Course> findByStatus(CourseStatus status,
                              Pageable pageable);

    Page<Course> findByStatusNot(CourseStatus status,
                              Pageable pageable);

    List<Course> findByConstructor_Email(String constructorEmail);

    Course findByCourseId(UUID courseId);
}
