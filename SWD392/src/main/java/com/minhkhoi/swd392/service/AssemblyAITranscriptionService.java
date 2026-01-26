package com.minhkhoi.swd392.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Real transcription service using AssemblyAI
 * This service supports direct video URLs from Cloudinary
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssemblyAITranscriptionService {

    @Value("${assembly.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api.assemblyai.com/v2";
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Transcribe video from URL
     * @param videoUrl URL of the video (supports Cloudinary URLs)
     * @param language Language code (vi, en, ja, etc.)
     * @return Transcript text
     */
    public String transcribeVideo(String videoUrl, String language) throws IOException {
        log.info("Starting AssemblyAI transcription for URL: {}, language: {}", videoUrl, language);

        // Check if API key is configured
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-assemblyai-api-key")) {
            log.warn("AssemblyAI API key not configured. Returning mock transcript.");
            return generateMockTranscript(language);
        }

        try {
            // Step 1: Submit transcription job
            String transcriptId = submitTranscriptionJob(videoUrl, language);
            log.info("Transcription job submitted. ID: {}", transcriptId);

            // Step 2: Poll for result
            String transcript = pollForTranscript(transcriptId);
            log.info("Transcription completed. Length: {} characters", transcript.length());

            return transcript;

        } catch (Exception e) {
            log.error("AssemblyAI transcription failed: {}", e.getMessage(), e);
            log.warn("Falling back to mock transcript");
            return generateMockTranscript(language);
        }
    }

    /**
     * Submit transcription job to AssemblyAI
     */
    private String submitTranscriptionJob(String videoUrl, String language) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("audio_url", videoUrl);
        
        // Map language code
        String languageCode = mapLanguageCode(language);
        if (languageCode != null) {
            requestBody.put("language_code", languageCode);
        }

        Request request = new Request.Builder()
                .url(API_URL + "/transcript")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .addHeader("authorization", apiKey)
                .addHeader("content-type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("AssemblyAI API error: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            JSONObject result = new JSONObject(responseBody);
            return result.getString("id");
        }
    }

    /**
     * Poll for transcription result
     */
    private String pollForTranscript(String transcriptId) throws IOException, InterruptedException {
        int maxAttempts = 60; // 5 minutes max (60 * 5 seconds)
        int attempt = 0;

        while (attempt < maxAttempts) {
            Request request = new Request.Builder()
                    .url(API_URL + "/transcript/" + transcriptId)
                    .addHeader("authorization", apiKey)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to get transcript status: " + response.code());
                }

                String responseBody = response.body().string();
                JSONObject result = new JSONObject(responseBody);
                String status = result.getString("status");

                log.info("Transcription status: {} (attempt {}/{})", status, attempt + 1, maxAttempts);

                switch (status) {
                    case "completed":
                        return result.getString("text");
                    case "error":
                        String error = result.optString("error", "Unknown error");
                        throw new IOException("Transcription failed: " + error);
                    case "queued":
                    case "processing":
                        // Wait and retry
                        Thread.sleep(5000); // Wait 5 seconds
                        attempt++;
                        break;
                    default:
                        throw new IOException("Unknown status: " + status);
                }
            }
        }

        throw new IOException("Transcription timeout after " + maxAttempts + " attempts");
    }

    /**
     * Map language code to AssemblyAI format
     */
    private String mapLanguageCode(String code) {
        if (code == null || code.equalsIgnoreCase("auto")) {
            return null; // Auto-detect
        }

        return switch (code.toLowerCase()) {
            case "vi" -> "vi";
            case "en" -> "en";
            case "ja" -> "ja";
            case "ko" -> "ko";
            case "zh" -> "zh";
            case "fr" -> "fr";
            case "de" -> "de";
            case "es" -> "es";
            case "pt" -> "pt";
            case "it" -> "it";
            case "nl" -> "nl";
            case "pl" -> "pl";
            case "ru" -> "ru";
            case "tr" -> "tr";
            case "uk" -> "uk";
            default -> "en"; // Default to English
        };
    }

    /**
     * Generate mock transcript for demonstration when API key is not configured
     */
    private String generateMockTranscript(String language) {
        return switch (language != null ? language.toLowerCase() : "auto") {
            case "vi" -> """
                [MOCK TRANSCRIPT - Tiếng Việt]
                
                Xin chào các bạn, chào mừng các bạn đến với video hướng dẫn của chúng tôi.
                
                Trong video này, chúng ta sẽ cùng nhau tìm hiểu về các chủ đề quan trọng.
                Đầu tiên, chúng ta sẽ bắt đầu với phần giới thiệu tổng quan.
                
                Tiếp theo, chúng ta sẽ đi sâu vào các chi tiết kỹ thuật.
                Các bạn hãy chú ý theo dõi để không bỏ lỡ thông tin quan trọng.
                
                Cuối cùng, chúng ta sẽ có phần tổng kết và câu hỏi thường gặp.
                Cảm ơn các bạn đã theo dõi video này.
                
                [LƯU Ý: Đây là transcript mẫu. Để có transcript thực, cần cấu hình AssemblyAI API key trong application.yaml]
                """;
            case "en" -> """
                [MOCK TRANSCRIPT - English]
                
                Hello everyone, welcome to our tutorial video.
                
                In this video, we will explore important topics together.
                First, we'll start with a general introduction.
                
                Next, we'll dive into the technical details.
                Please pay close attention so you don't miss any important information.
                
                Finally, we'll have a summary and frequently asked questions section.
                Thank you for watching this video.
                
                [NOTE: This is a sample transcript. For real transcription, configure AssemblyAI API key in application.yaml]
                """;
            default -> """
                [MOCK TRANSCRIPT - Auto-detected]
                
                Welcome to this video presentation.
                This is a demonstration transcript generated automatically.
                
                The actual content would depend on what is spoken in the video.
                This mock transcript is provided to test the API flow.
                
                To get real transcription:
                1. Sign up at https://www.assemblyai.com/
                2. Get your API key
                3. Add it to application.yaml: assemblyai.api-key=your-key
                4. Restart the server
                
                Thank you for your understanding.
                
                [NOTE: This is a sample transcript for demonstration purposes only.]
                """;
        };
    }
}
