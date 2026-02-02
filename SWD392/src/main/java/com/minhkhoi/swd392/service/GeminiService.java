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

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    /**
     * Generate quiz questions from content using Google Gemini.
     *
     * @param content The video transcript or document content.
     * @return JSON string containing the generated questions
     */
    public String generateQuizQuestions(String content) {
        log.info("Calling Google Gemini AI for content size: {}", content.length());
        log.info("Using Gemini API Key: {}******", apiKey != null && apiKey.length() > 5 ? apiKey.substring(0, 5) : "NULL");

        try {
            // Build the prompt
            String prompt = """
                You are an expert educational AI.
                Based on the provided context, generate 5 multiple-choice quiz questions.
                
                CONTEXT:
                """ + content + """
                
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

            // Construct JSON Body for Gemini
            JSONObject part = new JSONObject();
            part.put("text", prompt);

            JSONObject contentObj = new JSONObject();
            contentObj.put("parts", new JSONArray().put(part));

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", new JSONArray().put(contentObj));

            // Execute HTTP Request
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());
            
            // Note: Google Gemini passes API Key via Query Param usually, or Header 'x-goog-api-key'
            okhttp3.HttpUrl.Builder urlBuilder = okhttp3.HttpUrl.parse(GEMINI_API_URL).newBuilder();
            urlBuilder.addQueryParameter("key", apiKey);

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(urlBuilder.build())
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("Google Gemini API Error: {} - {}", response.code(), errorBody);
                    throw new RuntimeException("Failed to call Gemini API: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                // Parse Gemini Response: candidates[0].content.parts[0].text
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
                
                return "[]"; // Fallback empty
            }

        } catch (Exception e) {
            log.error("Error generating quiz with Gemini", e);
            throw new RuntimeException("Error generating quiz: " + e.getMessage());
        }
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
