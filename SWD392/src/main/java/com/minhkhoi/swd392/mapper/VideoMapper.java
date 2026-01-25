package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.VideoUploadResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    @Mapping(target = "videoUrl", expression = "java((String) uploadResult.get(\"secure_url\"))")
    @Mapping(target = "publicId", expression = "java((String) uploadResult.get(\"public_id\"))")
    @Mapping(target = "duration", expression = "java(uploadResult.get(\"duration\") != null ? ((Number) uploadResult.get(\"duration\")).intValue() : null)")
    @Mapping(target = "format", expression = "java((String) uploadResult.get(\"format\"))")
    @Mapping(target = "fileSize", expression = "java(uploadResult.get(\"bytes\") != null ? ((Number) uploadResult.get(\"bytes\")).longValue() : null)")
    @Mapping(target = "success", constant = "true")
    @Mapping(source = "transcript", target = "transcript")
    @Mapping(source = "message", target = "message")
    VideoUploadResponse toVideoUploadResponse(Map<String, Object> uploadResult, String transcript, String message);
}
