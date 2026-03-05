package com.minhkhoi.swd392.entity;

import com.minhkhoi.swd392.constant.JlptLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;



/**
 * Câu hỏi dành cho bài kiểm tra trình độ tiếng Nhật (Placement Test).
 * Tách biệt hoàn toàn với entity Question (dành cho Quiz trong khóa học).
 */
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

    /** Nội dung câu hỏi */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Các lựa chọn đáp án, lưu dạng JSON.
     * Ví dụ: {"A": "おはよう", "B": "こんにちは", "C": "さようなら", "D": "ありがとう"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb", nullable = false)
    private Map<String, String> options;

    /** Đáp án đúng: "A", "B", "C" hoặc "D" */
    @Column(name = "correct_answer", length = 1, nullable = false)
    private String correctAnswer;

    /** Giải thích đáp án đúng */
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    /**
     * Chủ đề / phần ngữ pháp của câu hỏi.
     * Ví dụ: "Trợ từ", "Hán tự", "Động từ", "Tính từ", "Kính ngữ", "Đọc hiểu"
     */
    @Column(name = "topic", length = 100, nullable = false)
    private String topic;

    /**
     * Mức độ JLPT tương ứng với câu hỏi.
     * N5 (cơ bản nhất) → N1 (nâng cao nhất)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false)
    private JlptLevel jlptLevel;

    /** Nguồn tài liệu tham khảo (ví dụ: "Dekiru Nihongo - Unit 3") */
    @Column(name = "source", length = 200)
    private String source;

    /**
     * Loại câu hỏi:
     * READING  → câu hỏi đọc/ngữ pháp/từ vựng thông thường
     * LISTENING → câu hỏi nghe (có file audio đính kèm)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    @Builder.Default
    private QuestionType questionType = QuestionType.READING;

    /**
     * URL file MP3/audio trên Cloudinary.
     * Chỉ có giá trị khi questionType = LISTENING.
     */
    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    /**
     * ID của PlacementDocument mà câu hỏi này được sinh ra từ đó.
     * Giúp truy vết nguồn gốc.
     */
    @Column(name = "source_document_id")
    private UUID sourceDocumentId;

    /** Đánh dấu câu hỏi do AI sinh ra (true) hay seeded thủ công (false) */
    @Column(name = "ai_generated")
    @Builder.Default
    private Boolean aiGenerated = false;

    public enum QuestionType {
        READING,    // Câu hỏi text thông thường
        LISTENING   // Câu hỏi nghe (có audioUrl)
    }
}
