package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.request.CreateLessonRequest;
import com.minhkhoi.swd392.dto.response.LessonResponse;
import com.minhkhoi.swd392.mapper.LessonMapper;
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
import java.util.List;
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
    private final LessonMapper lessonMapper;

    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request) {
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));

        validateCourseMutable(module);

        Lesson lesson = lessonMapper.toLesson(request, module);

        if (module.getCourse().getStatus() == CourseStatus.EDITING) {
            lesson.setIsPending(true);
        }

        if (request.getVideoFile() == null || request.getVideoFile().isEmpty()) {
            throw new AppException(ErrorCode.VIDEO_REQUIRED);
        }
        
        Map<String, Object> uploadResult = cloudinaryService.uploadVideo(request.getVideoFile());
        lesson.setVideoUrl((String) uploadResult.get("secure_url"));
        lesson.setDuration(mapDuration(uploadResult));

        if (request.getDocumentFile() != null && !request.getDocumentFile().isEmpty()) {
            String contentType = request.getDocumentFile().getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                throw new AppException(ErrorCode.DOCUMENT_NOT_PDF);
            }
            
            Map<String, Object> docResult = cloudinaryService.uploadFile(request.getDocumentFile(), "raw");
            lesson.setDocumentUrl((String) docResult.get("secure_url"));
            
            String content = extractTextContent(request.getDocumentFile());
            lesson.setDocumentContent(content);
        }

        lesson = lessonRepository.save(lesson);

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

        return lessonMapper.toLessonResponse(lesson);
    }

    public List<LessonResponse> getLessonsByModule(UUID moduleId) {
        return lessonRepository.findByModule_ModuleId(moduleId).stream()
                .map(lessonMapper::toLessonResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        validateCourseMutable(lesson.getModule());

        if (lesson.getModule().getCourse().getStatus() == CourseStatus.EDITING) {
            lesson.setIsPendingDeletion(true);
            lessonRepository.save(lesson);
        } else {
            deleteCloudinaryResources(lesson);
            lessonRepository.delete(lesson);
        }
    }

    @Transactional
    public LessonResponse updateLesson(UUID lessonId, String title, Integer orderIndex, 
                                     MultipartFile videoFile, MultipartFile documentFile) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        validateCourseMutable(lesson.getModule());

        if (title != null && !title.isBlank()) {
            lesson.setTitle(title);
        }
        
        if (orderIndex != null) {
            lesson.setOrderIndex(orderIndex);
        }

        boolean videoUpdated = false;

        if (videoFile != null && !videoFile.isEmpty()) {
            if (lesson.getVideoUrl() != null) {
                String publicId = extractPublicIdFromUrl(lesson.getVideoUrl());
                if (publicId != null) cloudinaryService.deleteFile(publicId, "video");
            }
            
            Map<String, Object> uploadResult = cloudinaryService.uploadVideo(videoFile);
            lesson.setVideoUrl((String) uploadResult.get("secure_url"));
            lesson.setDuration(mapDuration(uploadResult));
            lesson.setTranscript(null);
            lesson.setQuizStatus(com.minhkhoi.swd392.constant.QuizStatus.NOT_STARTED);
            videoUpdated = true;
        }

        if (documentFile != null && !documentFile.isEmpty()) {
            if (lesson.getDocumentUrl() != null) {
                String publicId = extractPublicIdFromUrl(lesson.getDocumentUrl());
                if (publicId != null) cloudinaryService.deleteFile(publicId, "raw");
            }

            Map<String, Object> docResult = cloudinaryService.uploadFile(documentFile, "raw");
            lesson.setDocumentUrl((String) docResult.get("secure_url"));
        }

        lesson = lessonRepository.save(lesson);


        if (videoUpdated) {
            final UUID finalLessonId = lesson.getLessonId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    lessonAsyncService.processTranscription(finalLessonId);
                }
            });
        }

        return lessonMapper.toLessonResponse(lesson);
    }

    @Transactional
    public void deleteVideoFromLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        validateCourseMutable(lesson.getModule());

        if (lesson.getVideoUrl() != null) {
            String publicId = extractPublicIdFromUrl(lesson.getVideoUrl());
            if (publicId != null) {
                cloudinaryService.deleteFile(publicId, "video");
            }
            
            lesson.setVideoUrl(null);
            lesson.setDuration(null);
            lesson.setTranscript(null);
            
            lessonRepository.save(lesson);
        }
    }

    private void validateCourseMutable(Module module) {
        CourseStatus status = module.getCourse().getStatus();
        if (status != CourseStatus.DRAFT && status != CourseStatus.EDITING && status != CourseStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_UPDATE);
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

            String postUpload = url.split("/upload/")[1];

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

    public String getDownloadDocumentUrl(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getDocumentUrl() == null || lesson.getDocumentUrl().isEmpty()) {
            throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND);
        }

        return lesson.getDocumentUrl();
    }
}
