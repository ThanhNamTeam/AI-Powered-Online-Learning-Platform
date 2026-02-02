package com.minhkhoi.swd392.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiService {

    @org.springframework.beans.factory.annotation.Value("${gemini.api-key}")
    private String apiKey;
    // Sửa thành phiên bản mới nhất hiển thị trên màn hình của bạn
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.0-flash:generateContent";
    /**
     * Generate quiz questions from content using Google Gemini.
     *
     * @param content The video transcript or document content.
     * @return JSON string containing the generated questions
     */
    public String generateQuizQuestions(String content) {



        String mockJapaneseQuiz = """
            [
              {
                "content": "日本語の基本的な挨拶で正しいものはどれですか？",
                "options": {
                  "A": "おはようございます",
                  "B": "สวัสดี",
                  "C": "Hello",
                  "D": "Bonjour"
                },
                "correctAnswer": "A",
                "explanation": "「おはようございます」は日本語で朝の挨拶を意味します。丁寧な表現で、ビジネスシーンでもよく使われます。"
              },
              {
                "content": "平仮名「あ」の書き順で最初に書く画はどれですか？",
                "options": {
                  "A": "横線",
                  "B": "縦線",
                  "C": "斜め線",
                  "D": "曲線"
                },
                "correctAnswer": "A",
                "explanation": "「あ」の最初の画は横線から始まります。正しい書き順を覚えることは、美しい日本語の文字を書くために重要です。"
              },
              {
                "content": "次の敬語表現のうち、謙譲語はどれですか？",
                "options": {
                  "A": "いらっしゃる",
                  "B": "申し上げる",
                  "C": "お越しになる",
                  "D": "召し上がる"
                },
                "correctAnswer": "B",
                "explanation": "「申し上げる」は謙譲語で、自分の行為をへりくだって表現する敬語です。他の選択肢は尊敬語に分類されます。"
              },
              {
                "content": "日本の伝統的な文化で「茶道」を表す英語はどれですか？",
                "options": {
                  "A": "Ikebana",
                  "B": "Tea Ceremony",
                  "C": "Calligraphy",
                  "D": "Origami"
                },
                "correctAnswer": "B",
                "explanation": "「茶道」は英語で「Tea Ceremony」と表現されます。日本の伝統的な文化の一つで、抹茶を点てて客人に振る舞う儀式です。"
              },
              {
                "content": "「ありがとうございます」の意味として正しいものはどれですか？",
                "options": {
                  "A": "すみません",
                  "B": "さようなら",
                  "C": "感謝を表す表現",
                  "D": "謝罪を表す表現"
                },
                "correctAnswer": "C",
                "explanation": "「ありがとうございます」は感謝を表す丁寧な日本語表現です。日常生活やビジネスシーンで頻繁に使用されます。"
              }
            ]
            """;

        return mockJapaneseQuiz.trim();
    }

    private String cleanAiJsonResponse(String raw) {
        if (raw == null) return "[]";
        // Remove markdown code blocks if present
        return raw.replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private String extractTextFromGeminiResponse(String responseBody) {
        // Deprecated
        return "[]";
    }
}
