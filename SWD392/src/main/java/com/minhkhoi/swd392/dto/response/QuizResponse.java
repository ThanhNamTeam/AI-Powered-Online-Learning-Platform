package com.minhkhoi.swd392.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private UUID quizId;
    private String title;
    private UUID lessonId;
    private String lessonTitle;
    private LocalDateTime createdAt;
    private Integer totalQuestions;
    private List<QuestionResponse> questions;
}
