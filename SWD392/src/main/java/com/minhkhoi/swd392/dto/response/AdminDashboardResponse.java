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

    // ── Thống kê tổng quan ──────────────────────────────────────
    private long totalUsers;
    private long totalStudents;
    private long totalInstructors;
    private long totalCourses;
    private long totalApprovedCourses;
    private long totalPendingCourses;
    private long totalRejectedCourses;
    private BigDecimal totalRevenue;          // Tổng doanh thu (COMPLETED payments)
    private long totalEnrollments;

    // ── Biểu đồ tăng trưởng người dùng (6 tháng gần nhất) ───────
    private List<MonthlyUserStat> userGrowth;

    // ── Biểu đồ doanh thu (6 tháng gần nhất) ───────────────────
    private List<MonthlyRevenueStat> revenueGrowth;

    // ── Phân bố khóa học theo JLPT Level ────────────────────────
    private List<CourseLevelStat> courseByLevel;

    // ── Danh sách khóa học chờ duyệt ────────────────────────────
    private List<PendingCourseInfo> pendingCourses;

    // ─── Inner DTOs ─────────────────────────────────────────────

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
