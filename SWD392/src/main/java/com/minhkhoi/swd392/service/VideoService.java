package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.VideoUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final CloudinaryService cloudinaryService;
    private final AssemblyAITranscriptionService assemblyAITranscriptionService;

    /**
     * Upload video and generate transcript
     * This is the main orchestration method that:
     * 1. Uploads video to Cloudinary
     * 2. Gets the video URL
     * 3. Sends URL to AI for transcription
     * 4. Returns complete response
     */
    public VideoUploadResponse uploadVideoAndTranscribe(MultipartFile file) {
        log.info("Processing video upload and transcription: {}", file.getOriginalFilename());

        try {
            // Step 1: Upload video to Cloudinary
            log.info("Step 1: Uploading video to Cloudinary...");
            Map<String, Object> uploadResult = cloudinaryService.uploadVideo(file);
            
            String videoUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            Integer duration = uploadResult.get("duration") != null 
                ? ((Number) uploadResult.get("duration")).intValue() 
                : null;
            String format = (String) uploadResult.get("format");
            Long fileSize = uploadResult.get("bytes") != null 
                ? ((Number) uploadResult.get("bytes")).longValue() 
                : null;

            log.info("Video uploaded successfully. URL: {}", videoUrl);

            // Step 2: Generate transcript using AI
            log.info("Step 2: Generating transcript with AI...");
            String transcript;
            try {
                transcript = assemblyAITranscriptionService.transcribeVideo(videoUrl, "auto");
                log.info("Transcript generated successfully. Length: {} characters", transcript.length());
            } catch (Exception e) {
                log.error("Failed to generate transcript, but video was uploaded: {}", e.getMessage());
                transcript = "Transcript generation failed: " + e.getMessage();
            }

            // Step 3: Build and return response
            return VideoUploadResponse.builder()
                    .success(true)
                    .videoUrl(videoUrl)
                    .publicId(publicId)
                    .transcript(transcript)
                    .duration(duration)
                    .format(format)
                    .fileSize(fileSize)
                    .message("Video uploaded and transcribed successfully")
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload video: {}", e.getMessage(), e);
            return VideoUploadResponse.builder()
                    .success(false)
                    .message("Failed to upload video: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during video processing: {}", e.getMessage(), e);
            return VideoUploadResponse.builder()
                    .success(false)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Upload video only (without transcription)
     */
    public VideoUploadResponse uploadVideoOnly(MultipartFile file) {
        log.info("Uploading video without transcription: {}", file.getOriginalFilename());

        try {
            Map<String, Object> uploadResult = cloudinaryService.uploadVideo(file);
            
            String videoUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            Integer duration = uploadResult.get("duration") != null 
                ? ((Number) uploadResult.get("duration")).intValue() 
                : null;
            String format = (String) uploadResult.get("format");
            Long fileSize = uploadResult.get("bytes") != null 
                ? ((Number) uploadResult.get("bytes")).longValue() 
                : null;

            return VideoUploadResponse.builder()
                    .success(true)
                    .videoUrl(videoUrl)
                    .publicId(publicId)
                    .duration(duration)
                    .format(format)
                    .fileSize(fileSize)
                    .message("Video uploaded successfully")
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload video: {}", e.getMessage(), e);
            return VideoUploadResponse.builder()
                    .success(false)
                    .message("Failed to upload video: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Delete video from Cloudinary
     */
    public void deleteVideo(String publicId) throws IOException {
        log.info("Deleting video: {}", publicId);
        cloudinaryService.deleteVideo(publicId);
    }

}
