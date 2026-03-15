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
    
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    private final java.util.concurrent.Semaphore semaphore = new java.util.concurrent.Semaphore(1);


    public String generateQuizQuestions(String content) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.length() < 10) {
            log.warn("API Key is missing or invalid. Returning Mock Data immediately.");
            return getMockJapaneseQuizData();
        }

        String finalContent = content;
        if (finalContent.length() > 4000) {
            log.info("Content too long ({}), truncating to 4000 chars.", finalContent.length());
            finalContent = finalContent.substring(0, 4000) + "... [truncated]";
        }

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.error("Semaphore acquire interrupted", e);
            Thread.currentThread().interrupt();
            return getMockJapaneseQuizData();
        }

        try {
            int maxRetries = 3;
            int retryCount = 0;
            long backoff = 2000;

            while (true) {
                try {
                    String prompt = """
You are an expert educational AI.
Based on the provided context, generate 10 multiple-choice quiz questions.

CONTEXT:
""" + finalContent + """

REQUIREMENTS:
1. Return ONLY a valid JSON array.
2. Each object in the array must follow this schema:
{
"content": "Question text here",
"options": {
"A": "Option A",
"B": "Option B",
"C": "Option C",
"D": "Option D"
},
"correctAnswer": "A", // or B, C, D
"explanation": "Brief explanation of why this answer is correct"
}
3. Ensure options are distinct and the correct answer is accurate according to the text.
""";

                    JSONObject part = new JSONObject();
                    part.put("text", prompt);

                    JSONObject contentObj = new JSONObject();
                    contentObj.put("parts", new JSONArray().put(part));

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("contents", new JSONArray().put(contentObj));

                    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                            .build();

                    okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
                    okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());

                    okhttp3.HttpUrl.Builder urlBuilder = okhttp3.HttpUrl.parse(GEMINI_API_URL).newBuilder();
                    urlBuilder.addQueryParameter("key", apiKey);

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(urlBuilder.build())
                            .post(body)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    try (okhttp3.Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            JSONArray candidates = jsonResponse.optJSONArray("candidates");
                            if (candidates != null && !candidates.isEmpty()) {
                                JSONObject firstCandidate = candidates.getJSONObject(0);
                                JSONObject contentRes = firstCandidate.optJSONObject("content");
                                if (contentRes != null) {
                                    JSONArray parts = contentRes.optJSONArray("parts");
                                    if (parts != null && !parts.isEmpty()) {
                                        String generatedText = parts.getJSONObject(0).optString("text");
                                        return cleanAiJsonResponse(generatedText);
                                    }
                                }
                            }
                            return "[]"; 
                        }

                        if (response.code() == 429) {
                            if (retryCount < maxRetries) {
                                log.warn("Gemini API Rate Limit (429). Retrying {}/{} in {}ms...", retryCount + 1, maxRetries, backoff);
                                Thread.sleep(backoff);
                                backoff *= 2;
                                retryCount++;
                                continue;
                            } else {
                                log.error("Gemini API Quota Exhausted after {} retries. Fallback to Mock Data.", maxRetries);
                                return getMockJapaneseQuizData();
                            }
                        }

                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        log.error("Google Gemini API Error: {} - {}", response.code(), errorBody);
                        throw new RuntimeException("Failed to call Gemini API: " + response.code() + " - " + errorBody);
                    }

                } catch (Exception e) {
                    log.error("Error generating quiz with Gemini (Attempt {}): {}", retryCount + 1, e.getMessage());
                    
                    if (retryCount < maxRetries) {
                        try { Thread.sleep(backoff); } catch (InterruptedException ex) {}
                        backoff *= 2;
                        retryCount++;
                    } else {
                        log.error("Final failure generating quiz. Fallback to Mock Data.");
                        return getMockJapaneseQuizData();
                    }
                }
            }
        } finally {
            semaphore.release();
            log.info("Semaphore released.");
        }
    }

    public String callGeminiWithPrompt(String prompt) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.length() < 10) {
            log.warn("[Gemini] API Key không hợp lệ.");
            throw new RuntimeException("Gemini API key not configured");
        }

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Semaphore interrupted", e);
        }

        try {
            int maxRetries = 3;
            int retryCount = 0;
            long backoff = 2000;

            while (true) {
                try {
                    JSONObject part = new JSONObject();
                    part.put("text", prompt);

                    JSONObject contentObj = new JSONObject();
                    contentObj.put("parts", new JSONArray().put(part));

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("contents", new JSONArray().put(contentObj));

                    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                            .build();

                    okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
                    okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());

                    okhttp3.HttpUrl.Builder urlBuilder = okhttp3.HttpUrl.parse(GEMINI_API_URL).newBuilder();
                    urlBuilder.addQueryParameter("key", apiKey);

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(urlBuilder.build())
                            .post(body)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    try (okhttp3.Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject jsonResp = new JSONObject(responseBody);
                            JSONArray candidates = jsonResp.optJSONArray("candidates");
                            if (candidates != null && !candidates.isEmpty()) {
                                JSONObject first = candidates.getJSONObject(0);
                                JSONObject resContent = first.optJSONObject("content");
                                if (resContent != null) {
                                    JSONArray parts = resContent.optJSONArray("parts");
                                    if (parts != null && !parts.isEmpty()) {
                                        return cleanAiJsonResponse(parts.getJSONObject(0).optString("text"));
                                    }
                                }
                            }
                            return "{}";
                        }

                        if (response.code() == 429) {
                            if (retryCount < maxRetries) {
                                log.warn("[Gemini] Rate limit 429. Retry {}/{} in {}ms", retryCount + 1, maxRetries, backoff);
                                Thread.sleep(backoff);
                                backoff *= 2;
                                retryCount++;
                                continue;
                            }
                            throw new RuntimeException("Gemini quota exhausted after " + maxRetries + " retries");
                        }

                        String errBody = response.body() != null ? response.body().string() : "unknown";
                        throw new RuntimeException("Gemini API error " + response.code() + ": " + errBody);
                    }
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    if (retryCount < maxRetries) {
                        log.warn("[Gemini] Error attempt {}: {}", retryCount + 1, e.getMessage());
                        try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        backoff *= 2;
                        retryCount++;
                    } else {
                        throw new RuntimeException("Gemini call failed after retries: " + e.getMessage(), e);
                    }
                }
            }
        } finally {
            semaphore.release();
        }
    }

    private String getMockJapaneseQuizData() {
        return """
            [
              {
                "content": "日本語の基本的な挨拶で正しいものはどれですか？ (Mock Data)",
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
                "explanation": "「あ」の書き順の1画目は横線です。"
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
                "explanation": "「申し上げる」は「言う」の謙譲語です。他は尊敬語です。"
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
                "explanation": "茶道は英語で Tea Ceremony と呼ばれます。"
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
                "explanation": "感謝の気持ちを伝える言葉です。"
              },
              {
                "content": "食事を始める前の挨拶はどれですか？",
                "options": {
                  "A": "ごちそうさまでした",
                  "B": "いただきます",
                  "C": "いってきます",
                  "D": "ただいま"
                },
                "correctAnswer": "B",
                "explanation": "食べる前には「いただきます」と言います。"
              },
              {
                "content": "薄い紙やシャツを数える時の助数詞はどれですか？",
                "options": {
                  "A": "本 (Hon)",
                  "B": "枚 (Mai)",
                  "C": "個 (Ko)",
                  "D": "匹 (Hiki)"
                },
                "correctAnswer": "B",
                "explanation": "薄いものを数える時は「枚（まい）」を使います。"
              },
              {
                "content": "「山」という漢字の読み方はどれですか？",
                "options": {
                  "A": "かわ",
                  "B": "うみ",
                  "C": "やま",
                  "D": "そら"
                },
                "correctAnswer": "C",
                "explanation": "「山」は「やま (Yama)」と読みます。"
              },
              {
                "content": "日本の首都はどこですか？",
                "options": {
                  "A": "大阪 (Osaka)",
                  "B": "京都 (Kyoto)",
                  "C": "東京 (Tokyo)",
                  "D": "広島 (Hiroshima)"
                },
                "correctAnswer": "C",
                "explanation": "日本の首都は東京です。"
              },
              {
                "content": "「さようなら」の意味はなんですか？",
                "options": {
                  "A": "Good morning",
                  "B": "Good night",
                  "C": "Goodbye",
                  "D": "Hello"
                },
                "correctAnswer": "C",
                "explanation": "「さようなら」は別れの挨拶です。"
              }
            ]
            """;
    }



    private String cleanAiJsonResponse(String raw) {

        if (raw == null) return "[]";


        return raw.replace("```json", "")

                .replace("```", "")

                .trim();

    }



    private String extractTextFromGeminiResponse(String responseBody) {


        return "[]";

    }

}
