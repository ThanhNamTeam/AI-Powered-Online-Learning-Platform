package com.minhkhoi.swd392.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AITranscriptionService {

    @Value("${google-ai.api-key}")
    private String apiKey;

    @Value("${google-ai.model}")
    private String model;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Generate transcript from video URL using Google Gemini API
     * @param videoUrl URL of the video to transcribe
     * @return Transcript text
     */
    public String transcribeVideo(String videoUrl) throws IOException {
        log.info("Starting video transcription for URL: {}", videoUrl);

        try {
            // Build the request to Gemini API
            String requestBody = buildTranscriptionRequest(videoUrl);
            String apiUrl = GEMINI_API_URL + model + ":generateContent?key=" + apiKey;

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(
                            requestBody,
                            MediaType.parse("application/json")
                    ))
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error details";
                    log.error("Gemini API error: {} - {}", response.code(), errorBody);
                    throw new IOException("Gemini API request failed: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                return parseTranscriptResponse(responseBody);
            }

        } catch (Exception e) {
            log.error("Failed to transcribe video: {}", e.getMessage(), e);
            throw new IOException("Failed to transcribe video: " + e.getMessage(), e);
        }
    }

    /**
     * Build JSON request for Gemini API
     */
    private String buildTranscriptionRequest(String videoUrl) {
        JSONObject request = new JSONObject();
        
        // Create contents array
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        
        // Create parts array with video file and prompt
        JSONArray parts = new JSONArray();
        
        // Add video file part
        JSONObject videoPart = new JSONObject();
        JSONObject fileData = new JSONObject();
        fileData.put("mimeType", "video/mp4");
        fileData.put("fileUri", videoUrl);
        videoPart.put("fileData", fileData);
        parts.put(videoPart);
        
        // Add text prompt
        JSONObject textPart = new JSONObject();
        textPart.put("text", 
            "Please transcribe this video accurately. " +
            "Provide the complete transcript of all spoken words in the video. " +
            "Format the transcript with proper punctuation and paragraph breaks. " +
            "If there are multiple speakers, indicate speaker changes. " +
            "Return only the transcript text without any additional commentary."
        );
        parts.put(textPart);
        
        content.put("parts", parts);
        contents.put(content);
        
        request.put("contents", contents);
        
        // Add generation config
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("topK", 32);
        generationConfig.put("topP", 1);
        generationConfig.put("maxOutputTokens", 8000);
        request.put("generationConfig", generationConfig);

        return request.toString();
    }

    /**
     * Parse transcript from Gemini API response
     */
    private String parseTranscriptResponse(String responseBody) throws IOException {
        try {
            JSONObject response = new JSONObject(responseBody);
            
            if (!response.has("candidates")) {
                throw new IOException("No candidates in response");
            }
            
            JSONArray candidates = response.getJSONArray("candidates");
            if (candidates.length() == 0) {
                throw new IOException("Empty candidates array");
            }
            
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject content = firstCandidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            
            if (parts.length() == 0) {
                throw new IOException("No parts in response");
            }
            
            String transcript = parts.getJSONObject(0).getString("text");
            log.info("Transcript generated successfully. Length: {} characters", transcript.length());
            
            return transcript.trim();
            
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            throw new IOException("Failed to parse transcript response: " + e.getMessage(), e);
        }
    }

    /**
     * Alternative: Transcribe using direct video file upload
     * This method first uploads the video to Gemini File API, then transcribes it
     */
    public String transcribeVideoWithUpload(byte[] videoBytes, String mimeType) throws IOException {
        log.info("Transcribing video with file upload (size: {} bytes)", videoBytes.length);
        
        // Note: This is a simplified version. In production, you would:
        // 1. Upload file to Gemini File API
        // 2. Get file URI
        // 3. Use that URI for transcription
        
        throw new UnsupportedOperationException(
            "Direct file upload transcription not yet implemented. " +
            "Please use transcribeVideo() with a public video URL instead."
        );
    }
}
