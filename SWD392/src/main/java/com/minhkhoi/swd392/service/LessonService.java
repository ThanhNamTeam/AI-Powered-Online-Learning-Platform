package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.request.CreateLessonRequest;
import com.minhkhoi.swd392.dto.response.LessonResponse;
import com.minhkhoi.swd392.entity.Lesson;
import com.minhkhoi.swd392.entity.Module;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.LessonRepository;
import com.minhkhoi.swd392.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final CloudinaryService cloudinaryService;
    private final LessonAsyncService lessonAsyncService;

    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request) {
        log.info("Creating new lesson: {}", request.getTitle());

        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .module(module)
                .build();

        // 1. Upload Video to Cloudinary (Mandatory)
        if (request.getVideoFile() == null || request.getVideoFile().isEmpty()) {
            throw new AppException(ErrorCode.VIDEO_REQUIRED);
        }
        
        Map<String, Object> uploadResult = cloudinaryService.uploadVideo(request.getVideoFile());
        lesson.setVideoUrl((String) uploadResult.get("secure_url"));
        lesson.setDuration(mapDuration(uploadResult));

        // 2. Upload Document (Optional)
        if (request.getDocumentFile() != null && !request.getDocumentFile().isEmpty()) {
            Map<String, Object> docResult = cloudinaryService.uploadFile(request.getDocumentFile(), "raw");
            lesson.setDocumentUrl((String) docResult.get("secure_url"));
            
            // Try to extract content if it's a text file
            String content = extractTextContent(request.getDocumentFile());
            lesson.setDocumentContent(content);
        }

        lesson = lessonRepository.save(lesson);

        // 3. Trigger Async Transcription for video AFTER transaction commits
        if (lesson.getVideoUrl() != null) {
            final UUID lessonId = lesson.getLessonId();
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            lessonAsyncService.processTranscription(lessonId);
                        }
                    }
                );
            } else {
                lessonAsyncService.processTranscription(lessonId);
            }
        }

        return LessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .title(lesson.getTitle())
                .videoUrl(lesson.getVideoUrl())
                .documentUrl(lesson.getDocumentUrl())
                .duration(lesson.getDuration())
                .build();
    }

    @Transactional
    public void deleteLesson(UUID lessonId) {
        log.info("Deleting lesson: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        // Delete from Cloudinary
        deleteCloudinaryResources(lesson);

        // Delete from DB (this automatically deletes transcript as it's a field in Lesson)
        lessonRepository.delete(lesson);
    }

    @Transactional
    public void deleteVideoFromLesson(UUID lessonId) {
        log.info("Deleting video from lesson: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getVideoUrl() != null) {
            String publicId = extractPublicIdFromUrl(lesson.getVideoUrl());
            if (publicId != null) {
                cloudinaryService.deleteFile(publicId, "video");
            }
            
            // Clear video info and TRANSCRIPT in DB
            lesson.setVideoUrl(null);
            lesson.setDuration(null);
            lesson.setTranscript(null); // Xóa transcript trong database như yêu cầu
            
            lessonRepository.save(lesson);
        }
    }

    private void deleteCloudinaryResources(Lesson lesson) {
        if (lesson.getVideoUrl() != null) {
            String publicId = extractPublicIdFromUrl(lesson.getVideoUrl());
            if (publicId != null) {
                cloudinaryService.deleteFile(publicId, "video");
            }
        }
        if (lesson.getDocumentUrl() != null) {
            String publicId = extractPublicIdFromUrl(lesson.getDocumentUrl());
            if (publicId != null) {
                cloudinaryService.deleteFile(publicId, "raw");
            }
        }
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("/upload/")) return null;
        try {
            // URL format: .../upload/v12345/folder/public_id.ext
            String postUpload = url.split("/upload/")[1];
            // parts[0] is version (v12345), we skin it and take everything else except extension
            int firstSlash = postUpload.indexOf("/");
            String pathWithExtension = postUpload.substring(firstSlash + 1);
            int lastDot = pathWithExtension.lastIndexOf(".");
            return pathWithExtension.substring(0, lastDot);
        } catch (Exception e) {
            log.warn("Failed to extract publicId from URL: {}", url);
            return null;
        }
    }

    private Integer mapDuration(Map<String, Object> map) {
        if (map == null) return null;
        Object duration = map.get("duration");
        return duration instanceof Number ? ((Number) duration).intValue() : null;
    }

    private String extractTextContent(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType != null && (contentType.equals("text/plain") || contentType.equals("application/json"))) {
                return new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
            }
            return "Content from " + file.getOriginalFilename() + " (Extraction not fully implemented for this type)";
        } catch (Exception e) {
            log.error("Failed to extract text content", e);
            return null;
        }
    }
}
