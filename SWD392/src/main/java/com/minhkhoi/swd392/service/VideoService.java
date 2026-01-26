package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.VideoUploadResponse;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final CloudinaryService cloudinaryService;
    private final AssemblyAITranscriptionService assemblyAITranscriptionService;
    private final VideoMapper videoMapper; // Inject Mapper

    public VideoUploadResponse uploadVideoAndTranscribe(MultipartFile file) {
        log.info("Processing video upload and transcription: {}", file.getOriginalFilename());

        // 1. Upload Video
        Map<String, Object> uploadResult;
        try {
            uploadResult = cloudinaryService.uploadVideo(file);
        } catch (Exception e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            // Ném ra Exception chuẩn hóa, GlobalExceptionHandler sẽ bắt lỗi này
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String videoUrl = (String) uploadResult.get("secure_url");
        log.info("Video uploaded successfully. URL: {}", videoUrl);

        // 2. Transcribe Video
        String transcript = null;
        try {
            log.info("Generating transcript with AI...");
            transcript = assemblyAITranscriptionService.transcribeVideo(videoUrl, "auto");
            log.info("Transcript generated successfully.");
        } catch (Exception e) {
            log.error("Transcription failed: {}", e.getMessage());
            transcript = "Transcript generation failed: " + e.getMessage();
        }

        // 3. Sử dụng Mapper để trả về kết quả
        return videoMapper.toResponse(uploadResult, transcript, "Video uploaded and transcribed successfully");
    }

    public VideoUploadResponse uploadVideoOnly(MultipartFile file) {
        log.info("Uploading video without transcription: {}", file.getOriginalFilename());

        try {
            Map<String, Object> uploadResult = cloudinaryService.uploadVideo(file);
            // Dùng Mapper
            return videoMapper.toResponse(uploadResult, "Video uploaded successfully");

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void deleteVideo(String publicId) {
        log.info("Deleting video: {}", publicId);
        try {
            cloudinaryService.deleteVideo(publicId);
        } catch (Exception e) {
            log.error("Delete video failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}