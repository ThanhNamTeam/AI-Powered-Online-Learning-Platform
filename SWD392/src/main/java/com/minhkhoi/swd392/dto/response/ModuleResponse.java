package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.entity.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleResponse {
    private UUID moduleId;
    private String title;
    private Integer orderIndex;

    private List<LessonResponse> lessons;

    public static ModuleResponse fromEntity(Module module) {
        return ModuleResponse.builder()
                .moduleId(module.getModuleId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .lessons(module.getLessons() != null ? module.getLessons().stream().map(LessonResponse::fromEntity).collect(Collectors.toList()) : null)
                .build();
    }
}
