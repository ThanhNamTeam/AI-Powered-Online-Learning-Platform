package com.minhkhoi.swd392.entity;

import com.minhkhoi.swd392.constant.JlptLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;


@Entity
@Table(name = "placement_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "placement_question_id")
    private UUID id;


    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb", nullable = false)
    private Map<String, String> options;

    @Column(name = "correct_answer", length = 1, nullable = false)
    private String correctAnswer;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "topic", length = 100, nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false)
    private JlptLevel jlptLevel;

    @Column(name = "source", length = 200)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    @Builder.Default
    private QuestionType questionType = QuestionType.READING;

    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    @Column(name = "source_document_id")
    private UUID sourceDocumentId;

    @Column(name = "ai_generated")
    @Builder.Default
    private Boolean aiGenerated = false;

    public enum QuestionType {
        READING,
        LISTENING
    }
}
