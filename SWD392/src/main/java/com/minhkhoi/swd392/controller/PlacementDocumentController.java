package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.placement.GenerateQuestionsResponse;
import com.minhkhoi.swd392.dto.placement.PlacementDocumentResponse;
import com.minhkhoi.swd392.entity.PlacementDocument.DocumentType;
import com.minhkhoi.swd392.service.PlacementDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/placement-documents")
@RequiredArgsConstructor
@Tag(
    name = "Placement Documents (Staff)",
    description = "Staff quản lý tài liệu ngân hàng câu hỏi. Upload PDF/DOC/MP3 lên Cloudinary. AI đọc và sinh câu hỏi."
)
@SecurityRequirement(name = "bearerAuth")
public class PlacementDocumentController {

    private final PlacementDocumentService placementDocumentService;
    @PostMapping(value = "/reading", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(
        summary = "Upload tài liệu PDF/DOC (STAFF)",
        description = """
            Staff upload file PDF hoặc DOC làm tài liệu ngân hàng câu hỏi.
            File sẽ được lưu lên Cloudinary.
            Sau đó dùng endpoint /generate-reading để AI sinh câu hỏi từ tài liệu này.
            
            **Hỗ trợ:** PDF, DOC, DOCX (tối đa 20MB)
            """
    )
    public ResponseEntity<PlacementDocumentResponse> uploadReadingDocument(
            @Parameter(description = "File PDF/DOC")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Tiêu đề tài liệu")
            @RequestParam("title") String title,
            @Parameter(description = "Mô tả nội dung tài liệu (tuỳ chọn)")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Cấp độ JLPT mục tiêu (N5/N4/N3/N2/N1, tuỳ chọn)")
            @RequestParam(value = "targetLevel", required = false) String targetLevel) {

        log.info("[PlacementDoc] Staff upload reading document: {}", file.getOriginalFilename());

        com.minhkhoi.swd392.dto.placement.UploadPlacementDocumentRequest request =
            com.minhkhoi.swd392.dto.placement.UploadPlacementDocumentRequest.builder()
                .title(title)
                .description(description)
                .documentType(DocumentType.READING)
                .targetLevel(targetLevel)
                .build();

        PlacementDocumentResponse response = placementDocumentService.uploadReadingDocument(file, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/listening", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(
        summary = "Upload file Audio MP3 dành cho câu hỏi nghe (STAFF)",
        description = """
            Staff upload file MP3, WAV, M4A hoặc OGG làm tài liệu câu hỏi nghe.
            File audio sẽ được lưu lên Cloudinary.
            Guest sẽ nghe file này và chọn đáp án.
            
            Sau đó dùng /generate-listening để AI sinh câu hỏi từ audio này.
            
            **Hỗ trợ:** MP3, WAV, M4A, OGG (tối đa 50MB)
            **Tỷ lệ câu nghe:** 25 câu → 3 câu nghe, 30 câu → 4, 40 câu → 6, 50 câu → 8
            """
    )
    public ResponseEntity<PlacementDocumentResponse> uploadListeningDocument(
            @Parameter(description = "File audio MP3/WAV/M4A")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Tiêu đề / chủ đề của đoạn nghe")
            @RequestParam("title") String title,
            @Parameter(description = "Mô tả nội dung đoạn nghe (AI dùng để sinh câu hỏi)")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Cấp độ JLPT mục tiêu (N5/N4/N3/N2/N1, tuỳ chọn)")
            @RequestParam(value = "targetLevel", required = false) String targetLevel) {

        log.info("[PlacementDoc] Staff upload listening document: {}", file.getOriginalFilename());

        com.minhkhoi.swd392.dto.placement.UploadPlacementDocumentRequest request =
            com.minhkhoi.swd392.dto.placement.UploadPlacementDocumentRequest.builder()
                .title(title)
                .description(description)
                .documentType(DocumentType.LISTENING)
                .targetLevel(targetLevel)
                .build();

        PlacementDocumentResponse response = placementDocumentService.uploadListeningDocument(file, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{documentId}/generate-reading")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(
        summary = "AI sinh câu hỏi text từ tài liệu PDF/DOC (STAFF)",
        description = """
            Trigger Gemini AI đọc tài liệu và sinh câu hỏi trắc nghiệm READING.
            AI sẽ sinh câu hỏi dựa trên tiêu đề và mô tả của tài liệu.
            
            **Lưu ý:** Chỉ áp dụng cho tài liệu loại READING (PDF/DOC).
            """
    )
    public ResponseEntity<GenerateQuestionsResponse> generateReadingQuestions(
            @Parameter(description = "ID của tài liệu Reading")
            @PathVariable UUID documentId,
            @Parameter(description = "Số câu muốn sinh (mặc định 10)")
            @RequestParam(defaultValue = "10") int questionCount) {

        log.info("[PlacementDoc] Trigger AI generate {} reading questions from doc {}", questionCount, documentId);
        GenerateQuestionsResponse response = placementDocumentService.generateQuestionsFromDocument(documentId, questionCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{documentId}/generate-listening")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(
        summary = "AI tạo câu hỏi nghe từ file audio (STAFF)",
        description = """
            AI sinh câu hỏi LISTENING dựa trên mô tả của file audio.
            File audio URL sẽ được đính kèm vào mỗi câu hỏi.
            Guest nghe file → chọn đáp án phù hợp.
            
            **Lưu ý:** Chỉ áp dụng cho tài liệu loại LISTENING (MP3/Audio).
            """
    )
    public ResponseEntity<GenerateQuestionsResponse> generateListeningQuestions(
            @Parameter(description = "ID của tài liệu Listening (audio)")
            @PathVariable UUID documentId,
            @Parameter(description = "Số câu nghe muốn tạo (mặc định 3)")
            @RequestParam(defaultValue = "3") int questionCount) {

        log.info("[PlacementDoc] Trigger create {} listening questions from audio doc {}", questionCount, documentId);
        GenerateQuestionsResponse response = placementDocumentService.createListeningQuestionsFromAudio(documentId, questionCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-mixed")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(
        summary = "AI trộn nhiều tài liệu → sinh câu hỏi tổng hợp (STAFF)",
        description = """
            AI lấy ngẫu nhiên tối đa 5 tài liệu READING đã được xử lý,
            trộn nội dung từ nhiều nguồn khác nhau và sinh bộ câu hỏi đa dạng.
            
            Giúp ngân hàng câu hỏi phong phú hơn, phù hợp với nhiều tài liệu giảng dạy.
            
            **Yêu cầu:** Phải có ít nhất 1 tài liệu READING đã được xử lý thành công.
            """
    )
    public ResponseEntity<GenerateQuestionsResponse> generateMixedQuestions(
            @Parameter(description = "Tổng số câu hỏi muốn sinh (mặc định 20)")
            @RequestParam(defaultValue = "20") int totalQuestions) {

        log.info("[PlacementDoc] Trigger AI mix documents → generate {} questions", totalQuestions);
        GenerateQuestionsResponse response = placementDocumentService.generateMixedQuestionsFromAllDocuments(totalQuestions);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(
        summary = "Lấy danh sách tài liệu placement test (STAFF)",
        description = "Trả về tất cả tài liệu đã upload. Lọc theo loại nếu cần."
    )
    public ResponseEntity<List<PlacementDocumentResponse>> getAllDocuments(
            @Parameter(description = "Lọc theo loại: READING hoặc LISTENING (tuỳ chọn)")
            @RequestParam(required = false) DocumentType type) {

        List<PlacementDocumentResponse> docs = type != null
                ? placementDocumentService.getDocumentsByType(type)
                : placementDocumentService.getAllDocuments();
        return ResponseEntity.ok(docs);
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(
        summary = "Xóa tài liệu (STAFF)",
        description = "Xóa tài liệu khỏi DB và Cloudinary. Lưu ý: câu hỏi đã sinh từ tài liệu này sẽ KHÔNG bị xóa."
    )
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID của tài liệu cần xóa")
            @PathVariable UUID documentId) {

        log.info("[PlacementDoc] Staff delete document id={}", documentId);
        placementDocumentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
