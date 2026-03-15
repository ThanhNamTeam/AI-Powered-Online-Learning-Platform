package com.minhkhoi.swd392.dto.placement;

import com.minhkhoi.swd392.entity.PlacementDocument;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPlacementDocumentRequest {

    @NotBlank(message = "Tiêu đề tài liệu không được để trống")
    private String title;

    private String description;

    private PlacementDocument.DocumentType documentType;

    private String targetLevel;
}
