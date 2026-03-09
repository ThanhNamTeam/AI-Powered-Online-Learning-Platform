package com.minhkhoi.swd392.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionRequest {

    @NotNull
    private UUID quizId;

    @NotEmpty
    private List<QuizAnswerItem> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizAnswerItem {
        @NotNull
        private UUID questionId;
        private String selectedAnswer;
    }
}
