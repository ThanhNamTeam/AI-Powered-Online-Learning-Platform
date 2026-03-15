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

    private long    totalCourses;
    private long    approvedCourses;
    private long    pendingCourses;
    private long    draftCourses;
    private long    rejectedCourses;
    private long    totalEnrollments;
    private BigDecimal totalRevenue;
    private BigDecimal currentMonthRevenue;
    private BigDecimal lastMonthRevenue;

    private List<MonthlyRevenueStat> revenueByMonth;


    private List<MonthlyEnrollmentStat> enrollmentByMonth;


    private List<CoursePerformance> topCourses;


    private List<MyCourseInfo> myCourses;


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
