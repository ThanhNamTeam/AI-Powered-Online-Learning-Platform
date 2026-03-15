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
public class StudentDashboardResponse {
    private String studentName;
    private Integer learningStreak;
    private Stats stats;
    private RecentCourse recentCourse;
    private List<RecommendedCourse> recommendedCourses;

    @Data
    @Builder
    public static class Stats {
        private String totalLearningTime;
        private Integer completionRate;
        private Double averageScore;
    }

    @Data
    @Builder
    public static class RecentCourse {
        private UUID courseId;
        private String title;
        private String currentLesson;
        private Integer progressPercentage;
        private Integer remainingMinutes;
        private String level;
    }

    @Data
    @Builder
    public static class RecommendedCourse {
        private UUID courseId;
        private String title;
        private String description;
        private String thumbnailUrl;
        private Double rating;
        private String constructorName;
        private Long price;
        private String status;
    }
}
