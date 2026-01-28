package com.minhkhoi.swd392.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateModuleRequest {
    private String title;
    private UUID courseId;
    private Integer orderIndex;
}
