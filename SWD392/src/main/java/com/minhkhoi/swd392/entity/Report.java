package com.minhkhoi.swd392.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id")
    private UUID reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_course_id")
    private Course reportedCourse;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType type;

    @Column(name = "report_title", length = 200, nullable = false)
    private String title;

    @Column(name = "report_description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by_admin_id")
    private User handledByAdmin;

    @Column(name = "report_admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @CreationTimestamp
    @Column(name = "report_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "report_resolved_at")
    private LocalDateTime resolvedAt;

    public enum ReportType {
        COURSE_QUALITY,
        INAPPROPRIATE_CONTENT,
        TECHNICAL_ISSUE,
        INSTRUCTOR_BEHAVIOR,
        PAYMENT_ISSUE,
        OTHER
    }

    public enum ReportStatus {
        PENDING,
        UNDER_REVIEW,
        RESOLVED,
        REJECTED,
        CLOSED
    }
}
