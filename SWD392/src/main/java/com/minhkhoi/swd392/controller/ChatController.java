package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chat (RAG)", description = "Sensei AI – trả lời dựa theo nội dung khóa học của student")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Hỏi Sensei AI (RAG)",
            description = "RAG Chat: AI trả lời dựa trên transcript và tài liệu từ các khóa học student đang học")
    public ResponseEntity<ApiResponse<Map<String, String>>> ask(
            @RequestBody Map<String, String> body) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String question = body.get("question");
        String lessonIdStr = body.get("lessonId");

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Câu hỏi không được để trống"));
        }

        UUID lessonId = null;
        if (lessonIdStr != null && !lessonIdStr.isBlank()) {
            try {
                lessonId = UUID.fromString(lessonIdStr);
            } catch (IllegalArgumentException e) {
                log.warn("[ChatController] Invalid lessonId: {}", lessonIdStr);
            }
        }

        log.info("[ChatController] email={} question={}", email, question);
        String answer = chatService.chat(email, question, lessonId);

        return ResponseEntity.ok(ApiResponse.success("OK",
                Map.of("answer", answer, "question", question)));
    }
}
