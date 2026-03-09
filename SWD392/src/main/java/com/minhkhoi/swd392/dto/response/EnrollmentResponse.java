package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.constant.EnrollmentStatus;
import com.minhkhoi.swd392.entity.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private UUID enrollmentId;
    private UserResponse student;
    private CourseResponse course;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus status;

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .student(UserResponse.fromEntity(enrollment.getUser()))
                .course(CourseResponse.fromEntity(enrollment.getCourse()))
                .enrolledAt(enrollment.getEnrolledAt())
                .status(enrollment.getStatus())
                .build();
    }
}
