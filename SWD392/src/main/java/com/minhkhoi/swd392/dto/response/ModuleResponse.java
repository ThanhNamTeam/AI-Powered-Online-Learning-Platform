package com.minhkhoi.swd392.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleResponse {
    private UUID moduleId;
    private String title;
    private Integer orderIndex;

    private List<LessonResponse> lessons;
}
