package com.minhkhoi.swd392.entity;

import com.minhkhoi.swd392.constant.JlptLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tài liệu bài test do Staff upload lên Cloudinary.
 * AI sẽ đọc các tài liệu này để sinh câu hỏi placement test.
 * Hỗ trợ 2 loại:
 *   - PDF/DOC: tài liệu text (đọc hiểu, ngữ pháp, từ vựng)
 *   - MP3/Audio: file nghe (câu hỏi nghe)
 */
@Entity
@Table(name = "placement_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id")
    private UUID id;

    /** Tên tài liệu (do Staff đặt) */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Mô tả nội dung tài liệu */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Nội dung TEXT thật đã extract từ file:
     *  - PDF/DOC  → Apache PDFBox/POI tự đọc
     *  - MP3/Audio → AssemblyAI transcript
     * Lưu vào DB để tái sử dụng, không cần đọc lại file.
     */
    @Column(name = "extracted_content", columnDefinition = "TEXT")
    private String extractedContent;

    /** URL file trên Cloudinary */
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    /** Public ID trên Cloudinary (để delete sau này) */
    @Column(name = "cloudinary_public_id", length = 500)
    private String cloudinaryPublicId;

    /** Loại file: PDF, DOC, DOCX, MP3, WAV, M4A */
    @Column(name = "file_type", length = 20, nullable = false)
    private String fileType;

    /**
     * Loại câu hỏi sẽ sinh ra từ tài liệu này:
     * READING  → PDF/DOC → câu hỏi trắc nghiệm text
     * LISTENING → MP3/Audio → câu hỏi nghe
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    @Builder.Default
    private DocumentType documentType = DocumentType.READING;

    /** Cấp độ JLPT mục tiêu của tài liệu */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_level")
    private JlptLevel targetLevel;

    /** Trạng thái: PENDING (chưa generate), PROCESSED (đã generate câu hỏi) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    /** Số câu hỏi đã được sinh từ tài liệu này */
    @Column(name = "generated_question_count")
    @Builder.Default
    private Integer generatedQuestionCount = 0;

    /** Staff upload tài liệu này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DocumentType {
        READING,    // PDF/DOC → câu hỏi text
        LISTENING   // MP3/Audio → câu hỏi nghe
    }

    public enum DocumentStatus {
        PENDING,     // Chưa generate câu hỏi
        PROCESSING,  // Đang được AI xử lý
        PROCESSED,   // Đã generate xong câu hỏi
        FAILED       // AI xử lý thất bại
    }
}
