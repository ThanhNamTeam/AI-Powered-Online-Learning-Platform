package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;

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
     * Upload any file to Cloudinary
     * @param file File to upload
     * @param resourceType "image", "video", or "raw"
     * @return Map containing upload result
     */
    public Map<String, Object> uploadFile(MultipartFile file, String resourceType) {
        log.info("Starting {} upload to Cloudinary: {}", resourceType, file.getOriginalFilename());
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "folder", folder,
                    "use_filename", true,
                    "unique_filename", true
                )
            );
            log.info("{} uploaded successfully. Public ID: {}", resourceType, uploadResult.get("public_id"));
            return uploadResult;
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * Upload video specifically (with validation)
     */
    public Map<String, Object> uploadVideo(MultipartFile file) {
        validateVideoFile(file);
        return uploadFile(file, "video");
    }

    /**
     * Delete file from Cloudinary
     * @param publicId Public ID of the file to delete
     * @param resourceType "image", "video", or "raw"
     */
    public void deleteFile(String publicId, String resourceType) {
        log.info("Deleting {} from Cloudinary: {}", resourceType, publicId);
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", resourceType)
            );
            
            String resultStatus = (String) result.get("result");
            if (!"ok".equals(resultStatus) && !"not found".equals(resultStatus)) {
                throw new IOException("Failed to delete file. Result: " + resultStatus);
            }
        } catch (IOException e) {
            log.error("Cloudinary delete failed", e);
            throw new AppException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
    }

    public String getUrl(String publicId, String resourceType) {
        return cloudinary.url()
            .resourceType(resourceType)
            .generate(publicId);
    }
}
