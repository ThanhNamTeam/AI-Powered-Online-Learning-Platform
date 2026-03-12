package com.minhkhoi.swd392.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDashboardResponse {

    private long newStudentsToday;
    private BigDecimal revenueToday;
    private long pendingRequests;
    private double averageRating; // Placeholder for now

    private List<WeeklyStat> weeklyPerformance;
    private List<TopCourseInfo> topTrendingCourses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyStat {
        private String day; // "Thứ 2", "Thứ 3", ...
        private long registrations;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopCourseInfo {
        private String courseId;
        private String title;
        private String code;
        private long students;
        private double rating;
    }
}
