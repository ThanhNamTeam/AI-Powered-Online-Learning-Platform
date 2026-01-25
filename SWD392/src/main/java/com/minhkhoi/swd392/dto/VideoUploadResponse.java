package com.minhkhoi.swd392.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoUploadResponse {
    private String videoUrl;
    private String publicId;
    private String transcript;
    private Integer duration;
    private String format;
    private Long fileSize;
    private String message;
    private boolean success;
}
