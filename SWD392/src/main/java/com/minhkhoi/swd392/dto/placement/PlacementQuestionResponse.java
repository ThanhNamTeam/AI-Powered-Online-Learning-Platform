package com.minhkhoi.swd392.dto.placement;

import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.entity.PlacementQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;


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
    private PlacementQuestion.QuestionType questionType;
    private String audioUrl;
}
