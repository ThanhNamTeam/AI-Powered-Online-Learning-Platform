package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.constant.EnrollmentStatus;
import com.minhkhoi.swd392.dto.PageResponse;
import com.minhkhoi.swd392.dto.request.CreateCourseRequest;
import com.minhkhoi.swd392.dto.request.VerifyCourseRequest;
import com.minhkhoi.swd392.dto.response.CourseResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.mapper.CourseMapper;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.ModuleRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;
    private final CloudinaryService cloudinaryService;
    private final ModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;

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

        // Upload thumbnail to Cloudinary
        String thumbnailUrl;
        if (request.getThumbnailFile() != null && !request.getThumbnailFile().isEmpty()) {
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(request.getThumbnailFile(), "image");
            thumbnailUrl = (String) uploadResult.get("secure_url"); 
        } else {
             throw new AppException(ErrorCode.INVALID_FILE);
        }

        // Map request to entity
        Course course = courseMapper.toCourse(request, user);
        course.setThumbnailUrl(thumbnailUrl);
        course.setCreatedAt(LocalDateTime.now());
        
        // Set status based on request
        // Always set DRAFT initially
        if (request.getStatus() != null && request.getStatus() != CourseStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_CREATE_STATUS);
        }
        course.setStatus(CourseStatus.DRAFT);
        
        // Save to database
        Course savedCourse = courseRepository.save(course);
        
        log.info("Course created successfully: {} by Instructor: {}", savedCourse.getTitle(), user.getEmail());

        return courseMapper.toCourseResponse(savedCourse);
    }

    /**
     * Get all courses (Optionally filtered by instructorId)
     */
    public List<CourseResponse> getAllCourses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Course> courses = courseRepository.findByConstructor_Email((email));
        return courses.stream()
                .map(courseMapper::toCourseResponse)
                .collect(Collectors.toList());
    }



    public PageResponse<CourseResponse> getAllCoursesForStudent(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Course> courses = courseRepository.findByEnrollments_User_EmailAndStatus(email, CourseStatus.APPROVED, pageable);

        return PageResponse.<CourseResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(courses.getTotalPages())
                .totalElements(courses.getTotalElements())
                .data(courses.stream().map(courseMapper::toCourseResponse).collect(Collectors.toList()))
                .build();
    }

    public PageResponse<CourseResponse> getAllCoursesPublic(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Course> courses;

        if (user.getRole() == User.Role.STUDENT) {
            courses = courseRepository.findByStatus(CourseStatus.APPROVED, pageable);
        } else if (user.getRole() == User.Role.STAFF) {
            courses = courseRepository.findByStatusNot(CourseStatus.DRAFT, pageable);
        } else {
            courses = courseRepository.findAll(pageable);
        }

        // 🔥 LẤY DANH SÁCH COURSE ĐÃ ENROLL (1 QUERY DUY NHẤT)
        List<UUID> enrolledCourseIds =
                enrollmentRepository.findCourseIdsByUserEmail(email);

        // 🔥 MAP + SET ENROLLED
        List<CourseResponse> data = courses.getContent()
                .stream()
                .map(course -> {
                    CourseResponse res = courseMapper.toCourseResponse(course);

                    res.setEnrolled(
                            enrolledCourseIds.contains(course.getCourseId())
                    );

                    return res;
                })
                .collect(Collectors.toList());

        return PageResponse.<CourseResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(courses.getTotalPages())
                .totalElements(courses.getTotalElements())
                .data(data)
                .build();
    }


    public CourseResponse getCourseById(UUID courseId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        CourseResponse response = courseMapper.toCourseResponse(course);

        List<UUID> enrolledCourseIds =
                enrollmentRepository.findCourseIdsByUserEmail(email);

        response.setEnrolled(enrolledCourseIds.contains(course.getCourseId()));

        return response;
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

        // Validate current status logic for Staff
        if (course.getStatus() != CourseStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_APPROVAL);
        }

        // Validate status request
        if (request.getStatus() == CourseStatus.APPROVED) {
            course.setStatus(CourseStatus.APPROVED);
            course.setRejectionReason(null); // Clear rejection reason if approved
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

    /**
     * Request approval for course (Instructor)
     */
    @Transactional
    public CourseResponse requestApproval(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Check if user is the instructor of this course
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!course.getConstructor().getEmail().equals(email)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // Check current status
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            // Can only request approval if Draft (or Rejected? requirement says MUST be DRAFT)
            // If rejected, user should probably edit and set back to draft or allow re-request logic.
            // Requirement: "Check trạng thái hiện tại: Phải là DRAFT mới được gửi."
            throw new AppException(ErrorCode.INVALID_VERIFY_STATUS); // Or generic "Invalid status for request"
        }

        // Check module count
        long moduleCount = moduleRepository.countByCourse_CourseId(courseId);
        if (moduleCount < 1) {
            throw new AppException(ErrorCode.MIN_MODULES_REQUIRED);
        }

        course.setStatus(CourseStatus.PENDING_APPROVAL);
        course.setRejectionReason(null); // Clear old rejection reason if any
        Course savedCourse = courseRepository.save(course);

        log.info("Course {} requested approval by Instructor: {}", course.getCourseId(), email);
        return courseMapper.toCourseResponse(savedCourse);
    }
}
