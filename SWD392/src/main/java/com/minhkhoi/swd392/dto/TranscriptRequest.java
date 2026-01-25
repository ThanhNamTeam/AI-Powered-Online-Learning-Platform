package com.minhkhoi.swd392.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptRequest {
    private String videoUrl;
    private String language;
}
