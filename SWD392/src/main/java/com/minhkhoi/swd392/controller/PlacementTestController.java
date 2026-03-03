package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.placement.PlacementQuestionResponse;
import com.minhkhoi.swd392.dto.placement.PlacementTestResultResponse;
import com.minhkhoi.swd392.dto.placement.SubmitPlacementTestRequest;
import com.minhkhoi.swd392.service.PlacementTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý bài kiểm tra trình độ tiếng Nhật.
 * TẤT CẢ endpoint là PUBLIC - không yêu cầu JWT Token.
 * Cho phép khách (guest) dùng tính năng mà không cần đăng nhập.
 */
@Slf4j
@RestController
@RequestMapping("/api/placement-test")
@RequiredArgsConstructor
@Tag(name = "Placement Test", description = "Kiểm tra trình độ tiếng Nhật miễn phí, không cần đăng nhập. AI tư vấn khóa học phù hợp.")
public class PlacementTestController {

    private final PlacementTestService placementTestService;

    /**
     * GET /api/placement-test/questions?count=25
     * Lấy bộ câu hỏi ngẫu nhiên để bắt đầu bài kiểm tra.
     */
    @GetMapping("/questions")
    @Operation(
            summary = "Lấy câu hỏi kiểm tra trình độ",
            description = "Trả về N câu hỏi ngẫu nhiên từ ngân hàng câu hỏi. Mặc định 25 câu. Không cần đăng nhập."
    )
    public ResponseEntity<List<PlacementQuestionResponse>> getQuestions(
            @Parameter(description = "Số lượng câu hỏi (tối đa 50, mặc định 25)")
            @RequestParam(defaultValue = "25") int count) {

        log.info("[PlacementTest] Lấy {} câu hỏi ngẫu nhiên", count);
        List<PlacementQuestionResponse> questions = placementTestService.getRandomQuestions(count);
        return ResponseEntity.ok(questions);
    }

    /**
     * POST /api/placement-test/submit
     * Nộp bài và nhận kết quả phân tích từ AI + gợi ý khóa học.
     */
    @PostMapping("/submit")
    @Operation(
            summary = "Nộp bài kiểm tra và nhận kết quả AI",
            description = """
                    Nộp danh sách đáp án. Server sẽ:
                    1. Chấm điểm tự động
                    2. Gửi kết quả cho Gemini AI phân tích chuyên sâu
                    3. Xác định trình độ JLPT (N5→N1)
                    4. Gợi ý các khóa học phù hợp
                    Không cần đăng nhập.
                    """
    )
    public ResponseEntity<PlacementTestResultResponse> submitTest(
            @Valid @RequestBody SubmitPlacementTestRequest request) {

        log.info("[PlacementTest] Nhận bài nộp với {} câu", request.getAnswers().size());
        PlacementTestResultResponse result = placementTestService.submitTest(request);
        log.info("[PlacementTest] Kết quả: {}/{} đúng, level ước tính: {}",
                result.getCorrectCount(), result.getTotalQuestions(), result.getEstimatedLevel());
        return ResponseEntity.ok(result);
    }
}
