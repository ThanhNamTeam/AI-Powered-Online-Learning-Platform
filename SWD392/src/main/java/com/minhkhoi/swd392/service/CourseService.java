package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.PageResponse;
import com.minhkhoi.swd392.dto.request.CreateCourseRequest;
import com.minhkhoi.swd392.dto.request.VerifyCourseRequest;
import com.minhkhoi.swd392.dto.response.CourseResponse;
import com.minhkhoi.swd392.dto.response.CourseStatsResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Lesson;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.mapper.CourseMapper;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.ModuleRepository;
import com.minhkhoi.swd392.repository.LessonRepository;
import com.minhkhoi.swd392.repository.ProgressRepository;
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
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, email));

        if (user.getRole() != User.Role.INSTRUCTOR) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        String thumbnailUrl;
        if (request.getThumbnailFile() != null && !request.getThumbnailFile().isEmpty()) {
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(request.getThumbnailFile(), "image");
            thumbnailUrl = (String) uploadResult.get("secure_url");
        } else {
            throw new AppException(ErrorCode.INVALID_FILE);
        }

        Course course = courseMapper.toCourse(request, user);
        course.setThumbnailUrl(thumbnailUrl);
        course.setCreatedAt(LocalDateTime.now());

        if (request.getStatus() != null && request.getStatus() != CourseStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_CREATE_STATUS);
        }
        course.setStatus(CourseStatus.DRAFT);

        Course savedCourse = courseRepository.save(course);

        log.info("Course created successfully: {} by Instructor: {}", savedCourse.getTitle(), user.getEmail());

        return courseMapper.toCourseResponse(savedCourse);
    }

    public List<CourseResponse> getAllCourses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Course> courses = courseRepository.findByConstructor_Email((email));
        return courses.stream()
                .map(courseMapper::toCourseResponse)
                .collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getAllCoursesForStudent(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Course> courses = courseRepository.findByEnrollments_User_EmailAndEnrollments_Status(
                email, com.minhkhoi.swd392.constant.EnrollmentStatus.ACTIVE, pageable);

        User user = userRepository.findByEmail(email).orElse(null);

        List<CourseResponse> result = courses.stream().map(course -> {
            CourseResponse resp = courseMapper.toCourseResponse(course);
            resp.setEnrolled(true);

            if (user != null) {
                enrollmentRepository
                        .findByUserAndCourse(user, course)
                        .ifPresent(enrollment -> {
                            long totalLessons     = progressRepository.countTotalLessonsByCourseId(course.getCourseId());
                            long completedLessons = progressRepository.countCompletedByEnrollment(enrollment);

                            int progressPct = (totalLessons > 0)
                                    ? (int) Math.round((double) completedLessons / totalLessons * 100)
                                    : 0;

                            resp.setTotalLessons((int) totalLessons);
                            resp.setCompletedLessons((int) completedLessons);
                            resp.setProgressPercentage(progressPct);

                            LocalDateTime maxUpdatedAt = progressRepository.findMaxUpdatedAtByEnrollment(enrollment).orElse(enrollment.getEnrolledAt());
                            resp.setLastAccessed(maxUpdatedAt);
                        });
            }
            return resp;
        }).collect(Collectors.toList());

        result.sort((c1, c2) -> {
            if (c1.getLastAccessed() == null && c2.getLastAccessed() == null) return 0;
            if (c1.getLastAccessed() == null) return 1;
            if (c2.getLastAccessed() == null) return -1;
            return c2.getLastAccessed().compareTo(c1.getLastAccessed());
        });

        return PageResponse.<CourseResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(courses.getTotalPages())
                .totalElements(courses.getTotalElements())
                .data(result)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getAllCoursesPublic(int page, int size, String search, String sortBy) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null) ? auth.getName() : "anonymousUser";
        User userLogged = userRepository.findByEmail(email).orElse(null);

        Page<Course> courses;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        String finalSearch = hasSearch ? search : null;

        if (userLogged != null && (userLogged.getRole() == User.Role.STAFF || userLogged.getRole() == User.Role.ADMIN)) {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.unsorted());
            if (hasSearch) {
                courses = courseRepository.findForStaffWithSearch(search, pageable);
            } else {
                courses = courseRepository.findForStaff(pageable);
            }
        } else {
            if ("newest".equalsIgnoreCase(sortBy)) {
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                courses = courseRepository.findByStatusAndTitleContainingIgnoreCase(CourseStatus.APPROVED, search != null ? search : "", pageable);
            } else if ("oldest".equalsIgnoreCase(sortBy)) {
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
                courses = courseRepository.findByStatusAndTitleContainingIgnoreCase(CourseStatus.APPROVED, search != null ? search : "", pageable);
            } else if ("rated".equalsIgnoreCase(sortBy)) {
                Pageable pageable = PageRequest.of(page - 1, size);
                courses = courseRepository.findTopRatedCourses(finalSearch, pageable);
            } else if ("recommended".equalsIgnoreCase(sortBy) || sortBy == null) {
                if (userLogged != null && userLogged.getEstimatedJlptLevel() != null) {
                    Pageable pageable = PageRequest.of(page - 1, size);
                    courses = courseRepository.findByStatusAndJlptLevelAndTitleContainingIgnoreCase(
                            CourseStatus.APPROVED, userLogged.getEstimatedJlptLevel(), search != null ? search : "", pageable);
                } else {
                    Pageable pageable = PageRequest.of(page - 1, size);
                    courses = courseRepository.findTopTrendingCourses(finalSearch, pageable);
                }
            } else {
                Pageable pageable = PageRequest.of(page - 1, size);
                courses = courseRepository.findTopTrendingCourses(finalSearch, pageable);
            }
        }

        return PageResponse.<CourseResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(courses.getTotalPages())
                .totalElements(courses.getTotalElements())
                .data(courses.getContent()
                        .stream()
                        .map(courseMapper::toCourseResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public CourseStatsResponse getCourseStats() {
        long pendingCount = courseRepository.countByStatusIn(java.util.List.of(
                CourseStatus.PENDING_APPROVAL,
                CourseStatus.PENDING_UPDATE,
                CourseStatus.PENDING_DELETION
        ));

        return CourseStatsResponse.builder()
                .pendingCount(pendingCount)
                .approvedCount(courseRepository.countByStatus(CourseStatus.APPROVED))
                .rejectedCount(courseRepository.countByStatus(CourseStatus.REJECTED))
                .totalCount(courseRepository.countByStatusNot(CourseStatus.DRAFT))
                .build();
    }


    public CourseResponse getCourseById(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        CourseResponse response = courseMapper.toCourseResponse(course);

        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email != null && !email.equals("anonymousUser")) {
                boolean isEnrolled = enrollmentRepository.existsByUser_EmailAndCourseAndStatusIn(
                        email, course,
                        List.of(
                                com.minhkhoi.swd392.constant.EnrollmentStatus.ACTIVE,
                                com.minhkhoi.swd392.constant.EnrollmentStatus.COMPLETED
                        )
                );
                response.setEnrolled(isEnrolled);
            } else {
                response.setEnrolled(false);
            }
        } catch (Exception e) {
            log.warn("Could not check enrollment status: {}", e.getMessage());
            response.setEnrolled(false);
        }

        return response;
    }


    @Transactional
    public CourseResponse verifyCourse(UUID courseId, VerifyCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        String staffEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User staffUser = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, staffEmail));

        if (course.getStatus() != CourseStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_APPROVAL);
        }

        if (request.getStatus() == CourseStatus.APPROVED) {
            course.setStatus(CourseStatus.APPROVED);
            course.setRejectionReason(null);
        } else if (request.getStatus() == CourseStatus.REJECTED) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new AppException(ErrorCode.MISSING_REJECTION_REASON);
            }
            course.setStatus(CourseStatus.REJECTED);
            course.setRejectionReason(request.getReason());
        } else {
            throw new AppException(ErrorCode.INVALID_VERIFY_STATUS);
        }

        course.setHandledByStaff(staffUser);

        Course savedCourse = courseRepository.save(course);
        log.info("Course {} verified with status: {} by Staff: {}",
                savedCourse.getCourseId(), savedCourse.getStatus(), staffUser.getEmail());

        return courseMapper.toCourseResponse(savedCourse);
    }

    @Transactional
    public CourseResponse requestApproval(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!course.getConstructor().getEmail().equals(email)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_VERIFY_STATUS);
        }

        long moduleCount = moduleRepository.countByCourse_CourseId(courseId);
        if (moduleCount < 1) {
            throw new AppException(ErrorCode.MIN_MODULES_REQUIRED);
        }

        course.setStatus(CourseStatus.PENDING_APPROVAL);
        course.setRejectionReason(null);
        Course savedCourse = courseRepository.save(course);

        log.info("Course {} requested approval by Instructor: {}", course.getCourseId(), email);
        return courseMapper.toCourseResponse(savedCourse);
    }

    @Transactional
    public CourseResponse submitUpdateRequest(UUID courseId, String updateNote) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!course.getConstructor().getEmail().equals(email)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (course.getStatus() != CourseStatus.EDITING) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_UPDATE);
        }

        course.setStatus(CourseStatus.PENDING_UPDATE);
        course.setPendingUpdateNote(updateNote);

        Course saved = courseRepository.save(course);
        log.info("Course {} submitted for update review by: {}", courseId, email);
        return courseMapper.toCourseResponse(saved);
    }

    @Transactional
    public CourseResponse requestUnlock(UUID courseId, String reason) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.APPROVED) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_UPDATE);
        }

        course.setStatus(CourseStatus.PENDING_UPDATE); 
        course.setPendingUpdateNote("REQUEST_UNLOCK: " + reason);

        return courseMapper.toCourseResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse reviewUpdateRequest(UUID courseId, String action, String reason) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PENDING_UPDATE) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_APPROVAL);
        }

        String staffEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User staffUser = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, staffEmail));

        if ("APPROVED".equalsIgnoreCase(action)) {
            processPendingChanges(course, true);

            course.setStatus(CourseStatus.APPROVED);
            course.setPendingUpdateNote(null);
            course.setRejectionReason(null);
        } else if ("REJECTED".equalsIgnoreCase(action)) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new AppException(ErrorCode.MISSING_REJECTION_REASON);
            }
            processPendingChanges(course, false);

            course.setStatus(CourseStatus.APPROVED);
            course.setRejectionReason(reason);
            course.setPendingUpdateNote(null);
        } else if ("UNLOCK".equalsIgnoreCase(action)) {
            course.setStatus(CourseStatus.EDITING);
            course.setPendingUpdateNote(null);
        } else {
            throw new AppException(ErrorCode.INVALID_VERIFY_STATUS);
        }

        course.setHandledByStaff(staffUser);
        Course saved = courseRepository.save(course);
        log.info("Course {} update request {} by Staff: {}", courseId, action, staffEmail);
        return courseMapper.toCourseResponse(saved);
    }


    private void processPendingChanges(Course course, boolean commit) {
        List<com.minhkhoi.swd392.entity.Module> modules = moduleRepository.findByCourse_CourseIdOrderByOrderIndexAsc(course.getCourseId());

        for (com.minhkhoi.swd392.entity.Module module : modules) {
            List<Lesson> lessons = lessonRepository.findByModule_ModuleId(module.getModuleId());
            for (Lesson lesson : lessons) {
                if (commit) {
                    if (Boolean.TRUE.equals(lesson.getIsPendingDeletion())) {
                        deleteCloudinaryResources(lesson);
                        lessonRepository.delete(lesson);
                    } else {
                        lesson.setIsPending(false);
                        lessonRepository.save(lesson);
                    }
                } else {
                    if (Boolean.TRUE.equals(lesson.getIsPending())) {
                        deleteCloudinaryResources(lesson);
                        lessonRepository.delete(lesson);
                    } else {
                        lesson.setIsPendingDeletion(false);
                        lessonRepository.save(lesson);
                    }
                }
            }
            if (commit) {
                if (Boolean.TRUE.equals(module.getIsPendingDeletion())) {
                    moduleRepository.delete(module);
                } else {
                    module.setIsPending(false);
                    moduleRepository.save(module);
                }
            } else {
                if (Boolean.TRUE.equals(module.getIsPending())) {
                    moduleRepository.delete(module);
                } else {
                    module.setIsPendingDeletion(false);
                    moduleRepository.save(module);
                }
            }
        }
    }

    private void deleteCloudinaryResources(Lesson lesson) {
        if (lesson.getVideoUrl() != null) {
            String publicId = extractPublicIdFromUrl(lesson.getVideoUrl());
            if (publicId != null) cloudinaryService.deleteFile(publicId, "video");
        }
        if (lesson.getDocumentUrl() != null) {
            String publicId = extractPublicIdFromUrl(lesson.getDocumentUrl());
            if (publicId != null) cloudinaryService.deleteFile(publicId, "raw");
        }
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("/upload/")) return null;
        try {
            String postUpload = url.split("/upload/")[1];
            int firstSlash = postUpload.indexOf("/");
            String pathWithExtension = postUpload.substring(firstSlash + 1);
            int lastDot = pathWithExtension.lastIndexOf(".");
            return pathWithExtension.substring(0, lastDot);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public CourseResponse requestDeletion(UUID courseId, String deletionNote) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!course.getConstructor().getEmail().equals(email)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (course.getStatus() != CourseStatus.APPROVED && course.getStatus() != CourseStatus.PENDING_UPDATE) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_DELETION);
        }

        course.setStatus(CourseStatus.PENDING_DELETION);
        course.setDeletionRequestNote(deletionNote != null ? deletionNote : "");

        Course saved = courseRepository.save(course);
        log.info("Course {} requested deletion by: {}", courseId, email);
        return courseMapper.toCourseResponse(saved);
    }

    @Transactional
    public CourseResponse reviewDeletionRequest(UUID courseId, String action, String reason) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PENDING_DELETION) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_APPROVAL);
        }

        String staffEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User staffUser = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, staffEmail));

        if ("APPROVED".equalsIgnoreCase(action)) {
            course.setStatus(CourseStatus.ARCHIVED);
            course.setDeletionRequestNote(null);
            course.setRejectionReason(null);
        } else if ("REJECTED".equalsIgnoreCase(action)) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new AppException(ErrorCode.MISSING_REJECTION_REASON);
            }
            course.setStatus(CourseStatus.APPROVED);
            course.setRejectionReason(reason);
            course.setDeletionRequestNote(null);
        } else {
            throw new AppException(ErrorCode.INVALID_VERIFY_STATUS);
        }

        course.setHandledByStaff(staffUser);
        Course saved = courseRepository.save(course);
        log.info("Course {} deletion request {} by Staff: {}", courseId, action, staffEmail);
        return courseMapper.toCourseResponse(saved);
    }
}