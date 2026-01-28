package com.minhkhoi.swd392.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponse {
    private UUID lessonId;
    private String title;
    private String videoUrl;
    private String documentUrl;
    private String transcript;
    private Integer duration;
}
