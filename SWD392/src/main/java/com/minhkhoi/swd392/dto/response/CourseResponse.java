package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.constant.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

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
}
