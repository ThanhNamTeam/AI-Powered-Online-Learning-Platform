package com.minhkhoi.swd392.dto.placement;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request nộp bài kiểm tra trình độ.
 * Client gửi danh sách {questionId, selectedAnswer} lên server.
 */
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
        /** ID của câu hỏi */
        @NotNull
        private UUID questionId;

        /**
         * Đáp án người dùng chọn: "A", "B", "C", "D".
         * Null nếu người dùng bỏ qua câu.
         */
        private String selectedAnswer;
    }
}
