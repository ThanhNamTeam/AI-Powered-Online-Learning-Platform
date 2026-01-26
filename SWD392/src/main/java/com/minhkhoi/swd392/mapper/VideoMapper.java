package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.VideoUploadResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    /**
     * Map từ kết quả Cloudinary + Transcript + Message sang Response
     */
    // 1. Dùng qualifiedByName = "mapString" cho các trường String lấy từ Map
    @Mapping(target = "videoUrl", source = "uploadResult.secure_url", qualifiedByName = "mapString")
    @Mapping(target = "publicId", source = "uploadResult.public_id", qualifiedByName = "mapString")
    @Mapping(target = "format", source = "uploadResult.format", qualifiedByName = "mapString")

    // 2. Dùng qualifiedByName riêng cho số (Duration, Size)
    @Mapping(target = "duration", source = "uploadResult", qualifiedByName = "mapDuration")
    @Mapping(target = "fileSize", source = "uploadResult", qualifiedByName = "mapFileSize")

    // 3. Các trường truyền thẳng không cần xử lý
    @Mapping(target = "transcript", source = "transcript")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "success", constant = "true")
    VideoUploadResponse toResponse(Map<String, Object> uploadResult, String transcript, String message);

    /**
     * Map cho trường hợp upload không có transcript
     */
    default VideoUploadResponse toResponse(Map<String, Object> uploadResult, String message) {
        return toResponse(uploadResult, null, message);
    }

    // --- CÁC HÀM HELPER (QUAN TRỌNG) ---

    // Đây là hàm giải quyết lỗi "Can't map property Object to String"
    @Named("mapString")
    default String mapString(Object value) {
        if (value == null) return null;
        return value instanceof String ? (String) value : value.toString();
    }

    @Named("mapDuration")
    default Integer mapDuration(Map<String, Object> map) {
        if (map == null) return null;
        Object duration = map.get("duration");
        return duration instanceof Number ? ((Number) duration).intValue() : null;
    }

    @Named("mapFileSize")
    default Long mapFileSize(Map<String, Object> map) {
        if (map == null) return null;
        Object bytes = map.get("bytes");
        return bytes instanceof Number ? ((Number) bytes).longValue() : null;
    }
}