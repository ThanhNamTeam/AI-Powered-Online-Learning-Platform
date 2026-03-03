package com.minhkhoi.swd392.dto.placement;

import com.minhkhoi.swd392.entity.PlacementDocument;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request để Staff tạo mới 1 tài liệu placement test (metadata).
 * File sẽ được upload riêng qua multipart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPlacementDocumentRequest {

    @NotBlank(message = "Tiêu đề tài liệu không được để trống")
    private String title;

    private String description;

    /**
     * Loại tài liệu: READING (PDF/DOC) hoặc LISTENING (MP3/Audio).
     * Mặc định READING nếu không truyền.
     */
    private PlacementDocument.DocumentType documentType;

    /** Level JLPT mục tiêu (N5, N4, N3, N2, N1) - tuỳ chọn */
    private String targetLevel;
}
