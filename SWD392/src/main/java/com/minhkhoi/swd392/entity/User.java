package com.minhkhoi.swd392.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.minhkhoi.swd392.constant.JlptLevel;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private String userId;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth_of_date")
    private LocalDate birthOfDate;

    @Column(name = "token_expiration_time")
    private LocalDateTime tokenExpirationTime;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimated_jlpt_level")
    private JlptLevel estimatedJlptLevel;

    @CreationTimestamp
    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "constructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> createdCourses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reportsMade;

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AISubscription> aiSubscriptions;

    public enum Role {
        ADMIN,
        INSTRUCTOR,
        STAFF,
        STUDENT
    }
}

