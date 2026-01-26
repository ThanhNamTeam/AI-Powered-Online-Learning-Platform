package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.VideoUploadResponse;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Management", description = "APIs for video upload and transcription")
public class VideoController {

    private final VideoService videoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload video and generate transcript",
            description = "Upload a video file to Cloudinary and automatically generate transcript using AI")
    public ResponseEntity<ApiResponse<VideoUploadResponse>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "transcribe", defaultValue = "true") boolean transcribe
    ) {
        log.info("Received video upload request: {} ({}MB), transcribe: {}",
                file.getOriginalFilename(),
                file.getSize() / (1024.0 * 1024.0),
                transcribe);

        // Gọi Service trực tiếp, không cần try-catch
        // Nếu lỗi xảy ra, Service sẽ ném AppException -> GlobalExceptionHandler sẽ bắt
        VideoUploadResponse response;
        if (transcribe) {
            response = videoService.uploadVideoAndTranscribe(file);
        } else {
            response = videoService.uploadVideoOnly(file);
        }

        // Luôn trả về thành công nếu code chạy đến đây
        return ResponseEntity.ok(ApiResponse.success("Video uploaded successfully", response));
    }

    @DeleteMapping
    @Operation(summary = "Delete video by request body",
            description = "Delete a video using public ID in request body (recommended)")
    public ResponseEntity<ApiResponse<Void>> deleteVideoByBody(@RequestBody DeleteRequest request) {
        log.info("Received video deletion request for public ID: {}", request.publicId());

        videoService.deleteVideo(request.publicId());

        return ResponseEntity.ok(ApiResponse.success("Video deleted successfully", null));
    }

    // Helper DTO (nên cân nhắc chuyển ra file riêng nếu dự án lớn)
    public record DeleteRequest(String publicId) {}
}