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
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalStudents;
    private long totalInstructors;
    private long totalCourses;
    private long totalApprovedCourses;
    private long totalPendingCourses;
    private long totalRejectedCourses;
    private BigDecimal totalRevenue;
    private long totalEnrollments;


    private List<MonthlyUserStat> userGrowth;

    private List<MonthlyRevenueStat> revenueGrowth;

    private List<CourseLevelStat> courseByLevel;

    private List<PendingCourseInfo> pendingCourses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyUserStat {
        private String month;       // "T1/26", "T2/26", ...
        private long students;
        private long instructors;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyRevenueStat {
        private String month;       // "T1/26", ...
        private BigDecimal revenue; // Tổng doanh thu tháng đó
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseLevelStat {
        private String level;   // "N5", "N4", "N3", "N2", "N1", "NONE"
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PendingCourseInfo {
        private String courseId;
        private String title;
        private String instructorName;
        private String jlptLevel;
        private BigDecimal price;
        private String createdAt;   // ISO string
    }
}
