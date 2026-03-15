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


    public Map<String, Object> uploadFile(MultipartFile file, String resourceType) {
        try {
            String originalFilename = file.getOriginalFilename();

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder", folder,
                            "use_filename", true,
                            "unique_filename", false,
                            "public_id", folder + "/" + originalFilename
                    )
            );

            return uploadResult;
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }


    public Map<String, Object> uploadVideo(MultipartFile file) {
        validateVideoFile(file);
        return uploadFile(file, "video");
    }


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


    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", folder));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Image upload failed", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
