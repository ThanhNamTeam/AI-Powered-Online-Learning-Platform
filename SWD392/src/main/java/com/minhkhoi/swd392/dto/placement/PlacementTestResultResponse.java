package com.minhkhoi.swd392.dto.placement;

import com.minhkhoi.swd392.constant.JlptLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response trả về sau khi nộp bài kiểm tra trình độ.
 * Bao gồm: điểm, nhận xét từ AI, và danh sách khóa học gợi ý.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementTestResultResponse {

    // ── Thông tin điểm ──────────────────────────────────────────────────────
    /** Số câu đúng */
    private int correctCount;

    /** Tổng số câu */
    private int totalQuestions;

    /** Điểm phần trăm (0–100) */
    private double scorePercent;

    // ── Kết quả phân tích từ AI ──────────────────────────────────────────────
    /** Mức trình độ AI đánh giá: N5, N4, N3, N2, N1 */
    private JlptLevel estimatedLevel;

    /** Tóm tắt tổng quan */
    private String overallComment;

    /** Danh sách điểm mạnh */
    private List<String> strengths;

    /** Danh sách điểm yếu */
    private List<String> weaknesses;

    /** Danh sách câu trả lời sai và phân tích */
    private List<WrongAnswerDetail> wrongAnswers;

    /** Khuyến nghị học tập */
    private String studyRecommendation;

    // ── Khóa học gợi ý ────────────────────────────────────────────────────────
    private List<SuggestedCourseCard> suggestedCourses;

    // ── Inner classes ─────────────────────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrongAnswerDetail {
        private int questionNumber;
        private String questionContent;
        private String topic;
        private JlptLevel jlptLevel;
        private String yourAnswer;
        private String correctAnswer;
        private String explanation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedCourseCard {
        private UUID courseId;
        private String title;
        private String description;
        private String thumbnailUrl;
        private BigDecimal price;
        private String level;
    }
}
