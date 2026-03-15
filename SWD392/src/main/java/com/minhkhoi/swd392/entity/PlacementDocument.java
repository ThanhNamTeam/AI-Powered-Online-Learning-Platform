package com.minhkhoi.swd392.entity;

import com.minhkhoi.swd392.constant.JlptLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


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


    @Column(name = "title", nullable = false, length = 255)
    private String title;


    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "extracted_content", columnDefinition = "TEXT")
    private String extractedContent;


    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;


    @Column(name = "cloudinary_public_id", length = 500)
    private String cloudinaryPublicId;


    @Column(name = "file_type", length = 20, nullable = false)
    private String fileType;


    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    @Builder.Default
    private DocumentType documentType = DocumentType.READING;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_level")
    private JlptLevel targetLevel;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;


    @Column(name = "generated_question_count")
    @Builder.Default
    private Integer generatedQuestionCount = 0;


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
        READING,
        LISTENING
    }

    public enum DocumentStatus {
        PENDING,
        PROCESSING,
        PROCESSED,
        FAILED
    }
}
