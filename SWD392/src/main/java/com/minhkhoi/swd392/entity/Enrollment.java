package com.minhkhoi.swd392.entity;

import com.minhkhoi.swd392.constant.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "enrollments_id")
    private UUID enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    private EnrollmentType type;

    @CreationTimestamp
    @Column(name = "enrollments_enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnrollmentStatus status;

    @Column(name = "enrollments_completed_at")
    private LocalDateTime completedAt;


    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Progress> progressList;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    public enum EnrollmentType {
        SUBSCRIPTION,
        SINGLE_PURCHASE
    }
}


