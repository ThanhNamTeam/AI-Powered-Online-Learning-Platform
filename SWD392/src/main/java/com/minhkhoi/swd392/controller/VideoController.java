package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.TranscriptRequest;
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

        VideoUploadResponse response;
        
        if (transcribe) {
            response = videoService.uploadVideoAndTranscribe(file);
        } else {
            response = videoService.uploadVideoOnly(file);
        }

        return ResponseEntity.ok(ApiResponse.success(
                response.getMessage() != null ? response.getMessage() : "Video processed successfully",
                response
        ));
    }

    @PostMapping("/transcript")
    @Operation(summary = "Generate transcript for existing video", 
               description = "Generate transcript for a video that's already uploaded (provide video URL)")
    public ResponseEntity<ApiResponse<TranscriptResponse>> generateTranscript(@RequestBody TranscriptRequest request) {
        log.info("Received transcript generation request for URL: {}", request.getVideoUrl());

        String transcript = videoService.generateTranscript(request.getVideoUrl());
        
        return ResponseEntity.ok(ApiResponse.success(
                "Transcript generated successfully",
                new TranscriptResponse(transcript)
        ));
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Delete video", 
               description = "Delete a video from Cloudinary using its public ID")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable String publicId) {
        log.info("Received video deletion request for public ID: {}", publicId);

        videoService.deleteVideo(publicId);
        return ResponseEntity.ok(ApiResponse.success(
                "Video deleted successfully",
                null
        ));
    }

    // Helper response classes
    record TranscriptResponse(String transcript) {}
}
