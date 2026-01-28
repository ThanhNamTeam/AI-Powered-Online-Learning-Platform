package com.minhkhoi.swd392.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLessonRequest {
    private String title;
    private UUID moduleId;
    private MultipartFile videoFile;
    private MultipartFile documentFile;
}
