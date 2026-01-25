package com.minhkhoi.swd392.entity;

import com.minhkhoi.swd392.constant.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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

    @Column(name = "course_title", length = 200, nullable = false)
    private String title;

    @Column(name = "course_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_price", precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_status")
    private CourseStatus status;

    @Column(name = "course_rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "course_thumbnail_url")
    private String thumbnailUrl;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Module> modules;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "reportedCourse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports;
}