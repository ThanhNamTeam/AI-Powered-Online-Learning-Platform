package com.minhkhoi.swd392.dto.placement;

import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.entity.PlacementQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * DTO trả về thông tin một câu hỏi cho client (cả READING và LISTENING).
 * KHÔNG trả về correctAnswer và explanation (tránh gian lận).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementQuestionResponse {

    private UUID questionId;
    private String content;
    private Map<String, String> options;
    private String topic;
    private JlptLevel jlptLevel;
    private String correctAnswer;


    /**
     * Loại câu hỏi: READING hoặc LISTENING.
     * Frontend dùng để hiển thị player nghe nếu là LISTENING.
     */
    private PlacementQuestion.QuestionType questionType;

    /**
     * URL file audio MP3 (chỉ có khi questionType = LISTENING).
     * Guest sẽ nghe file này rồi chọn đáp án.
     */
    private String audioUrl;
}
