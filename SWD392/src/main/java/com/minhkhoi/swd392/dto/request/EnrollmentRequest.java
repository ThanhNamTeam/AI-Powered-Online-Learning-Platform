package com.minhkhoi.swd392.dto.request;

import com.minhkhoi.swd392.entity.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {
    private String courseId;
    private Enrollment.EnrollmentType type;
}
