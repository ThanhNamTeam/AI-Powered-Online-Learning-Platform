package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.TranscriptRequest;
import com.minhkhoi.swd392.dto.VideoUploadResponse;
import com.minhkhoi.swd392.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public ResponseEntity<VideoUploadResponse> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "transcribe", defaultValue = "true") boolean transcribe
    ) {
        log.info("Received video upload request: {} ({}MB), transcribe: {}", 
                file.getOriginalFilename(), 
                file.getSize() / (1024.0 * 1024.0),
                transcribe);

        try {
            VideoUploadResponse response;
            
            if (transcribe) {
                response = videoService.uploadVideoAndTranscribe(file);
            } else {
                response = videoService.uploadVideoOnly(file);
            }

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("Error processing video upload: {}", e.getMessage(), e);
            VideoUploadResponse errorResponse = VideoUploadResponse.builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }




    @DeleteMapping
    @Operation(summary = "Delete video by request body", 
               description = "Delete a video using public ID in request body (recommended)")
    public ResponseEntity<?> deleteVideoByBody(@RequestBody DeleteRequest request) {
        log.info("Received video deletion request for public ID: {}", request.publicId());

        try {
            videoService.deleteVideo(request.publicId());
            return ResponseEntity.ok(new DeleteResponse(
                    true,
                    "Video deleted successfully"
            ));

        } catch (IOException e) {
            log.error("Error deleting video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DeleteResponse(
                            false,
                            "Failed to delete video: " + e.getMessage()
                    ));
        }
    }

    // Helper response classes
    record DeleteResponse(boolean success, String message) {}
    record DeleteRequest(String publicId) {}
}
