package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.request.CreateCourseRequest;
import com.minhkhoi.swd392.dto.request.VerifyCourseRequest;
import com.minhkhoi.swd392.dto.response.CourseResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.mapper.CourseMapper;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        // Get current user from Security Context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        // Check if user has INSTRUCTOR role
        if (user.getRole() != User.Role.INSTRUCTOR) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // Map request to entity
        Course course = courseMapper.toCourse(request, user);
        
        // Set status based on request
        if (request.getStatus() == CourseStatus.PENDING) {
            course.setStatus(CourseStatus.PENDING);
        } else {
            // Default is DRAFT (also applies if user sends DRAFT or null or illegal status)
            course.setStatus(CourseStatus.DRAFT);
        }
        
        // Save to database
        Course savedCourse = courseRepository.save(course);
        
        log.info("Course created successfully: {} by Instructor: {}", savedCourse.getTitle(), user.getEmail());

        return courseMapper.toCourseResponse(savedCourse);
    }

    /**
     * Get all courses (For STAFF)
     */
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toCourseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Verify course (Approve/Reject) (For STAFF)
     */
    @Transactional
    public CourseResponse verifyCourse(UUID courseId, VerifyCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Get current staff user from Security Context
        String staffEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User staffUser = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, staffEmail));

        // Validate status
        if (request.getStatus() == CourseStatus.PUBLISHED) {
            course.setStatus(CourseStatus.PUBLISHED);
            course.setRejectionReason(null); // Clear rejection reason if published
        } else if (request.getStatus() == CourseStatus.REJECTED) {
            // Require reason when rejecting
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new AppException(ErrorCode.MISSING_REJECTION_REASON);
            }
            course.setStatus(CourseStatus.REJECTED);
            course.setRejectionReason(request.getReason());
        } else {
            throw new AppException(ErrorCode.INVALID_VERIFY_STATUS);
        }

        // Set staff who handled this course
        course.setHandledByStaff(staffUser);

        Course savedCourse = courseRepository.save(course);
        log.info("Course {} verified with status: {} by Staff: {}", 
                savedCourse.getCourseId(), savedCourse.getStatus(), staffUser.getEmail());
        
        return courseMapper.toCourseResponse(savedCourse);
    }
}
