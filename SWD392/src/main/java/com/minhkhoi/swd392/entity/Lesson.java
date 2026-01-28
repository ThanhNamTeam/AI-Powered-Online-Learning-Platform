package com.minhkhoi.swd392.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lessons_id")
    private UUID lessonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modules_id", nullable = false)
    private Module module;

    @Column(name = "lessons_title", length = 200)
    private String title;

    @Column(name = "lessons_video_url")
    private String videoUrl;

    @Column(name = "lessons_video_duration")
    private Integer duration; // tính bằng giây

    @Column(name = "lessons_transcript", columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "lessons_document_url")
    private String documentUrl;

    @Column(name = "lessons_document_content", columnDefinition = "TEXT")
    private String documentContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_status")
    @Builder.Default
    private com.minhkhoi.swd392.constant.QuizStatus quizStatus = com.minhkhoi.swd392.constant.QuizStatus.NOT_STARTED;

    @Column(name = "last_quiz_error", columnDefinition = "TEXT")
    private String lastQuizError;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VectorStore> vectorStores;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Quiz> quizzes;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Progress> progressList;
}