package com.minhkhoi.swd392.dto.placement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsResponse {

    private UUID documentId;
    private String documentTitle;
    private int questionsGenerated;
    private List<String> generatedQuestionIds;
    private String message;
}
