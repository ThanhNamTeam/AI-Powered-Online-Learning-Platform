package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.dto.placement.PlacementQuestionResponse;
import com.minhkhoi.swd392.dto.placement.PlacementTestResultResponse;
import com.minhkhoi.swd392.dto.placement.PlacementTestResultResponse.SuggestedCourseCard;
import com.minhkhoi.swd392.dto.placement.PlacementTestResultResponse.WrongAnswerDetail;
import com.minhkhoi.swd392.dto.placement.SubmitPlacementTestRequest;
import com.minhkhoi.swd392.dto.placement.SubmitPlacementTestRequest.PlacementAnswerItem;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.PlacementQuestion;
import com.minhkhoi.swd392.entity.PlacementQuestion.QuestionType;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.PlacementQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementTestService {

    private static final int DEFAULT_QUESTION_COUNT = 25;

    private final PlacementQuestionRepository placementQuestionRepository;
    private final CourseRepository courseRepository;
    private final GeminiService geminiService;
    private final com.minhkhoi.swd392.repository.UserRepository userRepository;

    public List<PlacementQuestionResponse> getRandomQuestions(int count) {
        int limit = (count > 0 && count <= 50) ? count : DEFAULT_QUESTION_COUNT;

        int listeningCount = calculateListeningCount(limit);
        int readingCount   = limit - listeningCount;

        List<PlacementQuestion> listening = placementQuestionRepository
                .findRandomByQuestionType(QuestionType.LISTENING.name(), listeningCount);

        int actualListening  = listening.size();
        int actualReadingNeed = limit - actualListening;

        List<PlacementQuestion> reading = placementQuestionRepository
                .findRandomByQuestionType(QuestionType.READING.name(), actualReadingNeed);

        List<PlacementQuestion> mixed = new ArrayList<>();
        mixed.addAll(reading);
        mixed.addAll(listening);
        Collections.shuffle(mixed);

        return mixed.stream()
                .map(q -> PlacementQuestionResponse.builder()
                        .questionId(q.getId())
                        .content(q.getContent())
                        .options(q.getOptions())
                        .topic(q.getTopic())
                        .jlptLevel(q.getJlptLevel())
                        .questionType(q.getQuestionType())
                        .correctAnswer(q.getCorrectAnswer())
                        .audioUrl(q.getAudioUrl())
                        .build())
                .collect(Collectors.toList());
    }

    private int calculateListeningCount(int total) {
        if (total <= 0) return 0;
        if (total < 25) {
            return total >= 10 ? 1 : 0;
        }
        double ratio = 0.12 + (total - 25) * 0.0016;
        ratio = Math.min(ratio, 0.20);
        int count = (int) Math.floor(total * ratio);
        return Math.max(count, 3);
    }

    public PlacementTestResultResponse submitTest(SubmitPlacementTestRequest request) {
        List<PlacementAnswerItem> answers = request.getAnswers();
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Danh sách đáp án không được rỗng.");
        }

        List<UUID> questionIds = answers.stream()
                .map(PlacementAnswerItem::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<UUID, PlacementQuestion> questionMap = placementQuestionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(PlacementQuestion::getId, q -> q));

        int correctCount = 0;
        List<WrongAnswerDetail> wrongAnswers = new ArrayList<>();
        int questionNumber = 1;

        for (PlacementAnswerItem answer : answers) {
            PlacementQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                questionNumber++;
                continue;
            }

            boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(answer.getSelectedAnswer());
            if (isCorrect) {
                correctCount++;
            } else {
                wrongAnswers.add(WrongAnswerDetail.builder()
                        .questionNumber(questionNumber)
                        .questionContent(question.getContent())
                        .topic(question.getTopic())
                        .jlptLevel(question.getJlptLevel())
                        .yourAnswer(answer.getSelectedAnswer())
                        .correctAnswer(question.getCorrectAnswer())
                        .explanation(question.getExplanation())
                        .build());
            }
            questionNumber++;
        }

        int totalQuestions = Math.min(answers.size(), questionMap.size());
        double scorePercent = totalQuestions > 0
                ? Math.round((double) correctCount / totalQuestions * 100.0 * 10) / 10.0
                : 0.0;

        PlacementAiAnalysis aiAnalysis = analyzeWithGemini(correctCount, totalQuestions, scorePercent, wrongAnswers);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setEstimatedJlptLevel(aiAnalysis.estimatedLevel());
                userRepository.save(user);
            });
        }

        List<SuggestedCourseCard> suggestedCourses = findSuggestedCourses(aiAnalysis.estimatedLevel());

        return PlacementTestResultResponse.builder()
                .correctCount(correctCount)
                .totalQuestions(totalQuestions)
                .scorePercent(scorePercent)
                .estimatedLevel(aiAnalysis.estimatedLevel())
                .overallComment(aiAnalysis.overallComment())
                .strengths(aiAnalysis.strengths())
                .weaknesses(aiAnalysis.weaknesses())
                .wrongAnswers(wrongAnswers)
                .studyRecommendation(aiAnalysis.studyRecommendation())
                .suggestedCourses(suggestedCourses)
                .build();
    }

    private PlacementAiAnalysis analyzeWithGemini(
            int correctCount,
            int totalQuestions,
            double scorePercent,
            List<WrongAnswerDetail> wrongAnswers) {

        Map<String, Long> wrongTopics = wrongAnswers.stream()
                .collect(Collectors.groupingBy(WrongAnswerDetail::getTopic, Collectors.counting()));

        StringBuilder wrongSummary = new StringBuilder();
        for (WrongAnswerDetail w : wrongAnswers) {
            wrongSummary.append(String.format(
                    "- Câu %d [%s / Level %s]: Đúng là \"%s\", bạn chọn \"%s\". Giải thích: %s\n",
                    w.getQuestionNumber(),
                    w.getTopic(),
                    w.getJlptLevel(),
                    w.getCorrectAnswer(),
                    w.getYourAnswer() != null ? w.getYourAnswer() : "Bỏ qua",
                    w.getExplanation()
            ));
        }

        StringBuilder topicSummary = new StringBuilder();
        wrongTopics.forEach((topic, count) ->
                topicSummary.append(String.format("  • %s: %d câu sai\n", topic, count)));

        String prompt = String.format("""
                Bạn là chuyên gia đánh giá trình độ tiếng Nhật.
                Hãy phân tích kết quả bài kiểm tra trình độ của học viên và trả về JSON.
                
                KẾT QUẢ BÀI THI:
                - Số câu đúng: %d / %d
                - Điểm: %.1f%%
                
                CÁC CHỦ ĐỀ SAI NHIỀU:
                %s
                
                CHI TIẾT CÁC CÂU SAI:
                %s
                
                YÊU CẦU: Trả về JSON với cấu trúc SAU ĐÂY (không thêm text ngoài JSON):
                {
                  "estimatedLevel": "N5|N4|N3|N2|N1",
                  "overallComment": "Nhận xét tổng quan về trình độ học viên (tiếng Việt, 2-3 câu)",
                  "strengths": ["điểm mạnh 1", "điểm mạnh 2"],
                  "weaknesses": ["điểm yếu 1", "điểm yếu 2"],
                  "studyRecommendation": "Lời khuyên học tập chi tiết và lộ trình học (tiếng Việt, 3-5 câu)"
                }
                
                LƯU Ý khi đánh giá level:
                - N5: điểm < 40%% hoặc sai nhiều ở câu N5
                - N4: điểm 40-55%% hoặc nắm N5 nhưng yếu N4
                - N3: điểm 55-70%%
                - N2: điểm 70-85%%
                - N1: điểm > 85%%
                """,
                correctCount, totalQuestions, scorePercent,
                topicSummary.toString(),
                wrongSummary.toString()
        );

        try {
            String rawResponse = geminiService.callGeminiWithPrompt(prompt);
            return parseAiAnalysis(rawResponse, scorePercent);
        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini AI phân tích placement test: {}", e.getMessage());
            return buildFallbackAnalysis(scorePercent, wrongTopics);
        }
    }

    private PlacementAiAnalysis parseAiAnalysis(String rawJson, double scorePercent) {
        try {
            String cleanJson = rawJson
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JSONObject obj = new JSONObject(cleanJson);

            String levelStr = obj.optString("estimatedLevel", "N5");
            JlptLevel level;
            try {
                level = JlptLevel.valueOf(levelStr.toUpperCase());
            } catch (Exception e) {
                level = estimateLevelFromScore(scorePercent);
            }

            List<String> strengths = new ArrayList<>();
            JSONArray sArr = obj.optJSONArray("strengths");
            if (sArr != null) {
                for (int i = 0; i < sArr.length(); i++) strengths.add(sArr.getString(i));
            }

            List<String> weaknesses = new ArrayList<>();
            JSONArray wArr = obj.optJSONArray("weaknesses");
            if (wArr != null) {
                for (int i = 0; i < wArr.length(); i++) weaknesses.add(wArr.getString(i));
            }

            return new PlacementAiAnalysis(
                    level,
                    obj.optString("overallComment", "Phân tích trình độ dựa trên kết quả bài kiểm tra."),
                    strengths,
                    weaknesses,
                    obj.optString("studyRecommendation", "Tiếp tục ôn luyện đều đặn.")
            );
        } catch (Exception e) {
            log.warn("Không parse được JSON từ Gemini, dùng fallback.");
            return buildFallbackAnalysis(scorePercent, Collections.emptyMap());
        }
    }

    private PlacementAiAnalysis buildFallbackAnalysis(double scorePercent, Map<String, Long> wrongTopics) {
        JlptLevel level = estimateLevelFromScore(scorePercent);
        String topWeakness = wrongTopics.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Ngữ pháp");

        return new PlacementAiAnalysis(
                level,
                String.format("Bạn đạt %.1f%%. Trình độ ước tính: %s.", scorePercent, level),
                List.of("Đã hoàn thành bài kiểm tra"),
                List.of("Cần cải thiện: " + topWeakness),
                "Hãy tập trung ôn luyện các phần còn yếu và làm thêm bài tập thực hành."
        );
    }

    private JlptLevel estimateLevelFromScore(double score) {
        if (score >= 85) return JlptLevel.N1;
        if (score >= 70) return JlptLevel.N2;
        if (score >= 55) return JlptLevel.N3;
        if (score >= 40) return JlptLevel.N4;
        return JlptLevel.N5;
    }

    private List<SuggestedCourseCard> findSuggestedCourses(JlptLevel estimatedLevel) {
        List<JlptLevel> targetLevels = buildTargetLevels(estimatedLevel);

        List<Course> courses = courseRepository.findByStatusAndJlptLevelIn(
                CourseStatus.APPROVED, targetLevels);

        return courses.stream()
                .limit(6)
                .map(c -> SuggestedCourseCard.builder()
                        .courseId(c.getCourseId())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .thumbnailUrl(c.getThumbnailUrl())
                        .price(c.getPrice())
                        .level(c.getJlptLevel() != null ? c.getJlptLevel().name() : "N/A")
                        .build())
                .collect(Collectors.toList());
    }

    private List<JlptLevel> buildTargetLevels(JlptLevel currentLevel) {
        List<JlptLevel> levels = new ArrayList<>();
        levels.add(currentLevel);

        JlptLevel[] all = JlptLevel.values();
        int idx = currentLevel.ordinal();
        if (idx + 1 < all.length) {
            levels.add(all[idx + 1]);
        }
        return levels;
    }

    private record PlacementAiAnalysis(
            JlptLevel estimatedLevel,
            String overallComment,
            List<String> strengths,
            List<String> weaknesses,
            String studyRecommendation
    ) {}
}
