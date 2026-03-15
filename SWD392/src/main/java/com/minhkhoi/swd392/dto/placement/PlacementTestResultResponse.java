package com.minhkhoi.swd392.dto.placement;

import com.minhkhoi.swd392.constant.JlptLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementTestResultResponse {

    private int correctCount;

    private int totalQuestions;

    private double scorePercent;

    private JlptLevel estimatedLevel;

    private String overallComment;

    private List<String> strengths;

    private List<String> weaknesses;

    private List<WrongAnswerDetail> wrongAnswers;

    private String studyRecommendation;

    private List<SuggestedCourseCard> suggestedCourses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrongAnswerDetail {
        private int questionNumber;
        private String questionContent;
        private String topic;
        private JlptLevel jlptLevel;
        private String yourAnswer;
        private String correctAnswer;
        private String explanation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedCourseCard {
        private UUID courseId;
        private String title;
        private String description;
        private String thumbnailUrl;
        private BigDecimal price;
        private String level;
    }
}
