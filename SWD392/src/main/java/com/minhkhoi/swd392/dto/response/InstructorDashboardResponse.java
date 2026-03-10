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
public class InstructorDashboardResponse {

    // ── Thống kê tổng quan ──────────────────────────────────────────────────
    private long    totalCourses;           // Tổng số khóa học của GV
    private long    approvedCourses;        // Đã duyệt
    private long    pendingCourses;         // Chờ duyệt
    private long    draftCourses;           // Nháp
    private long    rejectedCourses;        // Bị từ chối
    private long    totalEnrollments;       // Tổng học viên đã ghi danh
    private BigDecimal totalRevenue;        // Tổng doanh thu (COMPLETED)
    private BigDecimal currentMonthRevenue; // Doanh thu tháng này
    private BigDecimal lastMonthRevenue;    // Doanh thu tháng trước

    // ── Biểu đồ doanh thu theo tháng (6 tháng gần nhất) ────────────────────
    private List<MonthlyRevenueStat> revenueByMonth;

    // ── Biểu đồ học viên mới ghi danh theo tháng (6 tháng gần nhất) ───────
    private List<MonthlyEnrollmentStat> enrollmentByMonth;

    // ── Top khóa học nổi bật (theo số học viên) ───────────────────────────
    private List<CoursePerformance> topCourses;

    // ── Danh sách khóa học của GV ──────────────────────────────────────────
    private List<MyCourseInfo> myCourses;

    // ─── Inner DTOs ─────────────────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MonthlyRevenueStat {
        private String month;
        private BigDecimal revenue;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MonthlyEnrollmentStat {
        private String month;
        private long   count;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CoursePerformance {
        private String     courseId;
        private String     title;
        private String     jlptLevel;
        private long       enrollmentCount;
        private BigDecimal revenue;
        private String     status;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MyCourseInfo {
        private String     courseId;
        private String     title;
        private String     jlptLevel;
        private String     status;
        private BigDecimal price;
        private String     thumbnailUrl;
        private String     createdAt;
        private long       enrollmentCount;
        private String     rejectionReason;
    }
}
