package com.minhkhoi.swd392.dto.placement;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitPlacementTestRequest {

    @NotNull
    @NotEmpty
    private List<PlacementAnswerItem> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlacementAnswerItem {
        @NotNull
        private UUID questionId;
        private String selectedAnswer;
    }
}
