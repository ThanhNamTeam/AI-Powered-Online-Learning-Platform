package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.entity.Lesson;
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

    public static LessonResponse fromEntity(Lesson lesson) {
        return LessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .title(lesson.getTitle())
                .videoUrl(lesson.getVideoUrl())
                .documentUrl(lesson.getDocumentUrl())
                .transcript(lesson.getTranscript())
                .duration(lesson.getDuration())
                .build();
    }
}
