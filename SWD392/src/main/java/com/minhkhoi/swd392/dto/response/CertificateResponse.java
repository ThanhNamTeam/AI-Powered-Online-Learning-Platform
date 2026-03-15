package com.minhkhoi.swd392.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private String studentName;
    private String courseTitle;
    private LocalDateTime completionDate;
    private UUID enrollmentId;
    private String certificateUrl; 
    private String message;
}
