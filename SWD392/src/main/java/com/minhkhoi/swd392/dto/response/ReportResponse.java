package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.entity.Report;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReportResponse {
    private UUID id;
    private String targetType;
    private String targetContent;
    private String targetLink;
    private String reason;
    private String reporter;
    private String reportedUser;
    private String timestamp;
    private String status;
    private String priority;

    public static ReportResponse fromEntity(Report report) {
        String tType = "KHÁC";
        if (report.getType() == Report.ReportType.INAPPROPRIATE_CONTENT) tType = "BÌNH LUẬN";
        if (report.getType() == Report.ReportType.COURSE_QUALITY) tType = "KHÓA HỌC";
        if (report.getType() == Report.ReportType.INSTRUCTOR_BEHAVIOR) tType = "NGƯỜI DÙNG";

        String targetCont = report.getTitle() != null ? report.getTitle() : "Báo cáo nội dung";
        if (report.getReportedCourse() != null) {
            targetCont = "Khóa học: " + report.getReportedCourse().getTitle();
            tType = "KHÓA HỌC";
        }

        String priority = "LOW";
        if (report.getType() == Report.ReportType.COURSE_QUALITY) priority = "HIGH";
        if (report.getType() == Report.ReportType.INAPPROPRIATE_CONTENT) priority = "CRITICAL";

        return ReportResponse.builder()
                .id(report.getReportId())
                .targetType(tType)
                .targetContent(targetCont)
                .targetLink(report.getReportedCourse() != null ? "/course/" + report.getReportedCourse().getCourseId() : "")
                .reason(report.getDescription())
                .reporter(report.getReporter() != null ? report.getReporter().getFullName() : "System")
                .reportedUser(report.getReportedUser() != null ? report.getReportedUser().getFullName() : "N/A")
                .timestamp(report.getCreatedAt().toString())
                .status(report.getStatus().name())
                .priority(priority)
                .build();
    }
}
