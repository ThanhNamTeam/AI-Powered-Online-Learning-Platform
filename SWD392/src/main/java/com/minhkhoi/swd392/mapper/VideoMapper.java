package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.VideoUploadResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VideoMapper {

    /**
     * Map từ kết quả Cloudinary + Transcript + Message sang Response
     */
    public VideoUploadResponse toResponse(Map<String, Object> uploadResult, String transcript, String message) {
        if (uploadResult == null) return null;

        return VideoUploadResponse.builder()
                .videoUrl(mapString(uploadResult.get("secure_url")))
                .publicId(mapString(uploadResult.get("public_id")))
                .format(mapString(uploadResult.get("format")))
                .duration(mapDuration(uploadResult))
                .fileSize(mapFileSize(uploadResult))
                .transcript(transcript)
                .message(message)
                .success(true)
                .build();
    }

    /**
     * Map cho trường hợp upload không có transcript
     */
    public VideoUploadResponse toResponse(Map<String, Object> uploadResult, String message) {
        return toResponse(uploadResult, null, message);
    }

    private String mapString(Object value) {
        if (value == null) return null;
        return value instanceof String ? (String) value : value.toString();
    }

    private Integer mapDuration(Map<String, Object> map) {
        if (map == null) return null;
        Object duration = map.get("duration");
        return duration instanceof Number ? ((Number) duration).intValue() : null;
    }

    private Long mapFileSize(Map<String, Object> map) {
        if (map == null) return null;
        Object bytes = map.get("bytes");
        return bytes instanceof Number ? ((Number) bytes).longValue() : null;
    }
}