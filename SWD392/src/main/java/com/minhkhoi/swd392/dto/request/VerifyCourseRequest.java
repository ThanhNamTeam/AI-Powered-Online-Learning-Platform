package com.minhkhoi.swd392.dto.request;

import com.minhkhoi.swd392.constant.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCourseRequest {
    
    @NotNull(message = "Status is required")
    private CourseStatus status;
    
    private String reason;
}
