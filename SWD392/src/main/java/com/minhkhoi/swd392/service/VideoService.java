package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.VideoUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.minhkhoi.swd392.mapper.VideoMapper;


import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final CloudinaryService cloudinaryService;
    private final AITranscriptionService aiTranscriptionService;
    private final VideoMapper videoMapper;

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


            // Step 1: Upload video to Cloudinary
            log.info("Step 1: Uploading video to Cloudinary...");
            Map<String, Object> uploadResult = cloudinaryService.uploadVideo(file);
            
            String videoUrl = (String) uploadResult.get("secure_url");
            log.info("Video uploaded successfully. URL: {}", videoUrl);

            // Step 2: Generate transcript using AI
            log.info("Step 2: Generating transcript with AI...");
            String transcript = aiTranscriptionService.transcribeVideo(videoUrl);
            log.info("Transcript generated successfully. Length: {} characters", transcript.length());

            // Step 3: Build and return response
            return videoMapper.toVideoUploadResponse(
                    uploadResult, 
                    transcript, 
                    "Video uploaded and transcribed successfully"
            );


    }

    /**
     * Upload video only (without transcription)
     */
    public VideoUploadResponse uploadVideoOnly(MultipartFile file) {
        log.info("Uploading video without transcription: {}", file.getOriginalFilename());


            Map<String, Object> uploadResult = cloudinaryService.uploadVideo(file);
            


            return videoMapper.toVideoUploadResponse(
                    uploadResult,
                    null,
                    "Video uploaded successfully"
            );


    }

    /**
     * Delete video from Cloudinary
     */
    public void deleteVideo(String publicId) {
        log.info("Deleting video: {}", publicId);
        cloudinaryService.deleteVideo(publicId);
    }

    /**
     * Generate transcript for existing video URL
     */
    public String generateTranscript(String videoUrl) {
        log.info("Generating transcript for existing video: {}", videoUrl);
        return aiTranscriptionService.transcribeVideo(videoUrl);
    }
}
