package com.minhkhoi.swd392.dto.response;

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
public class QuizSubmissionResponse {

    private UUID quizId;
    private String quizTitle;
    private Integer totalQuestions;
    private Integer correctAnswersCount;
    private Double score;
    
    /** Detailed review for each question */
    private List<QuestionReviewDetail> questionDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionReviewDetail {
        private UUID questionId;
        private String questionContent;
        private String yourAnswer;
        private String correctAnswer;
        private String explanation;
        private boolean isCorrect;
    }
}
