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
    private final VideoService videoService;
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

        // 1. Upload Video via VideoService to ensure consistent attributes
        if (request.getVideoFile() != null && !request.getVideoFile().isEmpty()) {
            com.minhkhoi.swd392.dto.VideoUploadResponse videoResponse = videoService.uploadVideoOnly(request.getVideoFile());
            lesson.setVideoUrl(videoResponse.getVideoUrl());
            lesson.setDuration(videoResponse.getDuration());
        }

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

    private String extractTextContent(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            // Simple extraction for text files
            if (contentType != null && (contentType.equals("text/plain") || contentType.equals("application/json"))) {
                return new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
            }
            // For others (PDF/DOCX), in a real app we'd use Tika/PDFBox
            // Here we just return a placeholder or empty
            return "Content from " + file.getOriginalFilename() + " (Extraction not fully implemented for this type)";
        } catch (Exception e) {
            log.error("Failed to extract text content", e);
            return null;
        }
    }
}
