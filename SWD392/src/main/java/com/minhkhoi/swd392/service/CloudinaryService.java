package com.minhkhoi.swd392.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    /**
     * Upload video to Cloudinary
     * @param file Video file to upload
     * @return Map containing upload result with URL, public_id, duration, etc.
     */
    public Map<String, Object> uploadVideo(MultipartFile file) throws IOException {
        log.info("Starting video upload to Cloudinary: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            validateVideoFile(file);
            
            // Upload with video-specific options
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", folder,
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false,
                    "quality", "auto",
                    "format", "mp4"
                )
            );
            
            log.info("Video uploaded successfully. Public ID: {}", uploadResult.get("public_id"));
            return uploadResult;
            
        } catch (IOException e) {
            log.error("Failed to upload video to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload video: " + e.getMessage(), e);
        }
    }

    /**
     * Delete video from Cloudinary
     * @param publicId Public ID of the video to delete
     * @throws IOException if deletion fails or video not found
     */
    public void deleteVideo(String publicId) throws IOException {
        log.info("Deleting video from Cloudinary: {}", publicId);
        
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", "video")
            );
            
            // Check result
            String resultStatus = (String) result.get("result");
            log.info("Cloudinary delete result: {}", result);
            
            if ("ok".equals(resultStatus)) {
                log.info("Video deleted successfully: {}", publicId);
            } else if ("not found".equals(resultStatus)) {
                throw new IOException("Video not found with public ID: " + publicId);
            } else {
                throw new IOException("Failed to delete video. Result: " + resultStatus);
            }
            
        } catch (IOException e) {
            log.error("Failed to delete video from Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Validate video file
     */
    private void validateVideoFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Check file size (max 100MB)
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IOException("File size exceeds maximum limit of 100MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IOException("File must be a video. Received: " + contentType);
        }

        log.info("Video file validated: {} ({}MB)", 
            file.getOriginalFilename(), 
            file.getSize() / (1024.0 * 1024.0));
    }

    /**
     * Get video URL from public ID
     */
    public String getVideoUrl(String publicId) {
        return cloudinary.url()
            .resourceType("video")
            .format("mp4")
            .generate(publicId);
    }
}
