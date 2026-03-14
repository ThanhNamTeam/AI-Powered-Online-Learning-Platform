package com.minhkhoi.swd392.entity;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.constant.JlptLevel;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "course_id")
    private UUID courseId;

    // FK trỏ về User (người tạo khóa học)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_constructor_id", nullable = false)
    private User constructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_handled_by_staff_id")
    private User handledByStaff;

    @Column(name = "course_title", length = 200, nullable = false)
    private String title;

    @Column(name = "course_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_price", precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_status")
    private CourseStatus status;

    @Column(name = "course_created_at", nullable = true)
    private LocalDateTime createdAt;

    @Column(name = "course_rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /** Ghi chú của Instructor khi yêu cầu cập nhật nội dung khóa học đã APPROVED */
    @Column(name = "course_pending_update_note", columnDefinition = "TEXT")
    private String pendingUpdateNote;

    /** Ghi chú của Instructor khi yêu cầu xóa khóa học */
    @Column(name = "course_deletion_request_note", columnDefinition = "TEXT")
    private String deletionRequestNote;

    /**
     * Cấp độ JLPT của khóa học (N5, N4, N3, N2, N1).
     * Dùng để gợi ý khóa học phù hợp từ kết quả Placement Test.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "course_jlpt_level")
    private JlptLevel jlptLevel;

    @Column(name = "course_thumbnail_url")
    private String thumbnailUrl;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Module> modules;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "reportedCourse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports;
}