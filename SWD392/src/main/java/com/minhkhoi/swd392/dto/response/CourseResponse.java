package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private UUID courseId;
    private String title;
    private String description;
    private BigDecimal price;
    private CourseStatus status;
    private String thumbnailUrl;
    private String rejectionReason;
    private UUID constructorId;
    private String constructorName;
    private UUID handledByStaffId;
    private String handledByStaffName;
    private String jlptLevel;
    private LocalDateTime createdAt;

    private String pendingUpdateNote;
    private String deletionRequestNote;

    private List<ModuleResponse> modules;
    private Boolean enrolled;
    private Integer progressPercentage;
    private Integer completedLessons;
    private Integer totalLessons;
    private LocalDateTime lastAccessed;

    public static CourseResponse fromEntity(Course course) {
        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .status(course.getStatus())
                .thumbnailUrl(course.getThumbnailUrl())
                .rejectionReason(course.getRejectionReason())
                .constructorId(UUID.fromString(course.getConstructor().getUserId()))
                .constructorName(course.getConstructor().getFullName())
                .handledByStaffId(course.getHandledByStaff() != null ? UUID.fromString(course.getHandledByStaff().getUserId()) : null)
                .handledByStaffName(course.getHandledByStaff() != null ? course.getHandledByStaff().getFullName() : null)
                .jlptLevel(course.getJlptLevel() != null ? course.getJlptLevel().name() : null)
                .createdAt(course.getCreatedAt())
                .pendingUpdateNote(course.getPendingUpdateNote())
                .deletionRequestNote(course.getDeletionRequestNote())
                .modules(course.getModules() != null ? course.getModules().stream().map(ModuleResponse::fromEntity).collect(Collectors.toList()) : null)
                .build();
    }
}