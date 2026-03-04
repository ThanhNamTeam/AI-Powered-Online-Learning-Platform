package com.minhkhoi.swd392.dto.placement;

import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.entity.PlacementDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO response thông tin tài liệu placement test.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDocumentResponse {

    private UUID documentId;
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private PlacementDocument.DocumentType documentType;
    private JlptLevel targetLevel;
    private PlacementDocument.DocumentStatus status;
    private Integer generatedQuestionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Tên Staff upload */
    private String uploadedByName;
}
