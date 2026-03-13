package com.minhkhoi.swd392.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "discussions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discussion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "discussion_id")
    private UUID discussionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DiscussionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DiscussionStatus status = DiscussionStatus.UNANSWERED;

    @Column(name = "likes")
    @Builder.Default
    private Integer likes = 0;

    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum DiscussionType {
        QUESTION,
        DISCUSSION
    }

    public enum DiscussionStatus {
        UNANSWERED,
        ANSWERED,
        READ
    }
}
