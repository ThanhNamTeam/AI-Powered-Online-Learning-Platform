package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.dto.placement.GenerateQuestionsResponse;
import com.minhkhoi.swd392.dto.placement.PlacementDocumentResponse;
import com.minhkhoi.swd392.dto.placement.UploadPlacementDocumentRequest;
import com.minhkhoi.swd392.entity.PlacementDocument;
import com.minhkhoi.swd392.entity.PlacementDocument.DocumentStatus;
import com.minhkhoi.swd392.entity.PlacementDocument.DocumentType;
import com.minhkhoi.swd392.entity.PlacementQuestion;
import com.minhkhoi.swd392.entity.PlacementQuestion.QuestionType;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.PlacementDocumentRepository;
import com.minhkhoi.swd392.repository.PlacementQuestionRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementDocumentService {

    private final PlacementDocumentRepository documentRepository;
    private final PlacementQuestionRepository questionRepository;
    private final CloudinaryService cloudinaryService;
    private final GeminiService geminiService;
    private final AssemblyAITranscriptionService assemblyAIService;
    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/x-wav",
            "audio/mp4", "audio/m4a", "audio/ogg"
    );

    private static final long MAX_DOCUMENT_SIZE = 20L * 1024 * 1024;  // 20MB
    private static final long MAX_AUDIO_SIZE    = 50L * 1024 * 1024;  // 50MB
    private static final int  MAX_TEXT_CHARS    = 6000;

    @Transactional
    public PlacementDocumentResponse uploadReadingDocument(
            MultipartFile file,
            UploadPlacementDocumentRequest request) {

        validateDocumentFile(file);

        String extractedText = extractTextFromDocument(file);
        log.info("[PlacementDoc] Extracted {} chars from '{}'", extractedText.length(), file.getOriginalFilename());

        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "raw");
        String fileUrl      = (String) uploadResult.get("secure_url");
        String cloudinaryId = (String) uploadResult.get("public_id");
        String extension    = getExtension(Objects.requireNonNull(file.getOriginalFilename())).toUpperCase();

        PlacementDocument doc = PlacementDocument.builder()
                .title(request.getTitle() != null ? request.getTitle() : file.getOriginalFilename())
                .description(request.getDescription())
                .fileUrl(fileUrl)
                .cloudinaryPublicId(cloudinaryId)
                .fileType(extension)
                .documentType(DocumentType.READING)
                .targetLevel(parseLevel(request.getTargetLevel()))
                .extractedContent(truncate(extractedText, MAX_TEXT_CHARS))
                .status(extractedText.isBlank() ? DocumentStatus.FAILED : DocumentStatus.PENDING)
                .uploadedBy(getCurrentUser())
                .build();

        documentRepository.save(doc);
        log.info("[PlacementDoc] Saved reading doc id={}, status={}", doc.getId(), doc.getStatus());
        return toResponse(doc);
    }

    @Transactional
    public PlacementDocumentResponse uploadListeningDocument(
            MultipartFile audioFile,
            UploadPlacementDocumentRequest request) {

        validateAudioFile(audioFile);

        String originalName = Objects.requireNonNull(audioFile.getOriginalFilename());
        String extension    = getExtension(originalName).toUpperCase();

        Map<String, Object> uploadResult = cloudinaryService.uploadFile(audioFile, "video");
        String fileUrl      = (String) uploadResult.get("secure_url");
        String cloudinaryId = (String) uploadResult.get("public_id");

        log.info("[PlacementDoc] Uploaded audio: {} → {}", originalName, fileUrl);

        PlacementDocument doc = PlacementDocument.builder()
                .title(request.getTitle() != null ? request.getTitle() : originalName)
                .description(request.getDescription())
                .fileUrl(fileUrl)
                .cloudinaryPublicId(cloudinaryId)
                .fileType(extension)
                .documentType(DocumentType.LISTENING)
                .targetLevel(parseLevel(request.getTargetLevel()))
                .status(DocumentStatus.PENDING)
                .uploadedBy(getCurrentUser())
                .build();

        documentRepository.save(doc);
        log.info("[PlacementDoc] Saved listening doc id={}", doc.getId());
        return toResponse(doc);
    }

    @Transactional
    public GenerateQuestionsResponse generateQuestionsFromDocument(UUID documentId, int questionCount) {
        PlacementDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        if (doc.getDocumentType() != DocumentType.READING) {
            throw new IllegalArgumentException("Chỉ generate READING questions từ tài liệu PDF/DOC.");
        }

        doc.setStatus(DocumentStatus.PROCESSING);
        documentRepository.save(doc);

        try {
            String contentForAI = buildReadingContentForAI(doc);
            String prompt       = buildReadingPrompt(doc, contentForAI, questionCount);
            String rawResponse  = geminiService.callGeminiWithPrompt(prompt);

            List<PlacementQuestion> questions = parseGeneratedQuestions(rawResponse, doc);

            if (questions.isEmpty()) {
                throw new RuntimeException("AI không sinh được câu hỏi nào. Phản hồi AI: " + rawResponse.substring(0, Math.min(200, rawResponse.length())));
            }

            questionRepository.saveAll(questions);

            doc.setStatus(DocumentStatus.PROCESSED);
            doc.setGeneratedQuestionCount(doc.getGeneratedQuestionCount() + questions.size());
            documentRepository.save(doc);

            log.info("[PlacementDoc] Generated {} reading questions from doc id={}", questions.size(), documentId);
            return buildResponse(doc, questions, "Đã sinh %d câu hỏi từ nội dung thật của \"%s\"!");

        } catch (Exception e) {
            doc.setStatus(DocumentStatus.FAILED);
            documentRepository.save(doc);
            log.error("[PlacementDoc] Generate reading failed for doc {}: {}", documentId, e.getMessage());
            throw new RuntimeException("AI generate câu hỏi thất bại: " + e.getMessage(), e);
        }
    }

    @Transactional
    public GenerateQuestionsResponse createListeningQuestionsFromAudio(UUID documentId, int questionCount) {
        PlacementDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        if (doc.getDocumentType() != DocumentType.LISTENING) {
            throw new IllegalArgumentException("Tài liệu này không phải loại LISTENING.");
        }

        doc.setStatus(DocumentStatus.PROCESSING);
        documentRepository.save(doc);

        try {

            String transcript = getOrCreateTranscript(doc);
            log.info("[PlacementDoc] Transcript length: {} chars for doc id={}", transcript.length(), documentId);

            String prompt = buildListeningPrompt(doc, transcript, questionCount);
            String rawResponse = geminiService.callGeminiWithPrompt(prompt);

            List<PlacementQuestion> questions = parseListeningQuestions(rawResponse, doc);

            if (questions.isEmpty()) {
                throw new RuntimeException("AI không sinh được câu hỏi nghe nào. Phản hồi AI: " + rawResponse.substring(0, Math.min(200, rawResponse.length())));
            }

            questionRepository.saveAll(questions);

            doc.setStatus(DocumentStatus.PROCESSED);
            doc.setGeneratedQuestionCount(doc.getGeneratedQuestionCount() + questions.size());
            documentRepository.save(doc);

            log.info("[PlacementDoc] Generated {} listening questions from audio doc id={}", questions.size(), documentId);
            return buildResponse(doc, questions, "Đã tạo %d câu hỏi nghe từ audio \"%s\" (AI đã nghe và hiểu nội dung thật)!");

        } catch (Exception e) {
            doc.setStatus(DocumentStatus.FAILED);
            documentRepository.save(doc);
            log.error("[PlacementDoc] Listening generate failed for doc {}: {}", documentId, e.getMessage());
            throw new RuntimeException("Tạo câu nghe thất bại: " + e.getMessage(), e);
        }
    }

    @Transactional
    public GenerateQuestionsResponse generateMixedQuestionsFromAllDocuments(int totalQuestions) {
        List<PlacementDocument> docs = documentRepository.findRandomDocumentsByType("READING", 5);

        if (docs.isEmpty()) {
            throw new IllegalStateException("Chưa có tài liệu READING nào. Vui lòng upload ít nhất 1 file PDF/DOC.");
        }

        log.info("[PlacementDoc] Trộn {} tài liệu để generate {} câu", docs.size(), totalQuestions);

        StringBuilder combinedContent = new StringBuilder();
        for (PlacementDocument d : docs) {
            String content = buildReadingContentForAI(d);
            combinedContent.append("\n\n--- Tài liệu: ").append(d.getTitle()).append(" ---\n");
            combinedContent.append(truncate(content, 1500)); // 1500 chars mỗi tài liệu
        }

        String prompt      = buildMixedPrompt(docs, combinedContent.toString(), totalQuestions);
        String rawResponse = geminiService.callGeminiWithPrompt(prompt);

        PlacementDocument primaryDoc = docs.get(0);
        List<PlacementQuestion> questions = parseGeneratedQuestions(rawResponse, primaryDoc);
        questionRepository.saveAll(questions);

        String docTitles = docs.stream().map(PlacementDocument::getTitle).collect(Collectors.joining(", "));

        return GenerateQuestionsResponse.builder()
                .documentId(primaryDoc.getId())
                .documentTitle("Tổng hợp từ: " + docTitles)
                .questionsGenerated(questions.size())
                .generatedQuestionIds(questions.stream().map(q -> q.getId().toString()).collect(Collectors.toList()))
                .message(String.format("AI đã trộn nội dung thật của %d tài liệu → sinh %d câu hỏi!", docs.size(), questions.size()))
                .build();
    }

    public List<PlacementDocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PlacementDocumentResponse> getDocumentsByType(DocumentType type) {
        return documentRepository.findByDocumentType(type).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        PlacementDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        if (doc.getCloudinaryPublicId() != null) {
            String resourceType = doc.getDocumentType() == DocumentType.LISTENING ? "video" : "raw";
            try {
                cloudinaryService.deleteFile(doc.getCloudinaryPublicId(), resourceType);
            } catch (Exception e) {
                log.warn("[PlacementDoc] Cloudinary delete failed: {}", e.getMessage());
            }
        }
        documentRepository.delete(doc);
        log.info("[PlacementDoc] Deleted doc id={}", documentId);
    }

    private String extractTextFromDocument(MultipartFile file) {
        String filename = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        try (InputStream is = file.getInputStream()) {
            if (filename.endsWith(".pdf")) {
                return extractFromPdf(file.getBytes());
            } else if (filename.endsWith(".docx")) {
                return extractFromDocx(is);
            } else if (filename.endsWith(".doc")) {
                return extractFromDoc(is);
            } else {

                return extractFromPdf(file.getBytes());
            }
        } catch (Exception e) {
            log.warn("[PlacementDoc] Cannot extract text from '{}': {}", filename, e.getMessage());
            return "";
        }
    }

    private String extractFromPdf(byte[] bytes) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");
            String text = stripper.getText(pdf);
            log.info("[PlacementDoc] PDFBox extracted {} chars from PDF ({} pages)", text.length(), pdf.getNumberOfPages());
            return text.trim();
        }
    }

    private String extractFromDocx(InputStream is) throws IOException {
        try (XWPFDocument docx = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            String text = extractor.getText();
            log.info("[PlacementDoc] POI extracted {} chars from DOCX", text.length());
            return text.trim();
        }
    }

    private String extractFromDoc(InputStream is) throws IOException {
        try (HWPFDocument doc = new HWPFDocument(is);
             WordExtractor extractor = new WordExtractor(doc)) {
            String text = extractor.getText();
            log.info("[PlacementDoc] POI extracted {} chars from DOC", text.length());
            return text.trim();
        }
    }

    private String getOrCreateTranscript(PlacementDocument doc) {
        if (doc.getExtractedContent() != null && !doc.getExtractedContent().isBlank()) {
            log.info("[PlacementDoc] Dùng transcript đã cache cho doc id={}", doc.getId());
            return doc.getExtractedContent();
        }

        log.info("[PlacementDoc] Transcribing audio via AssemblyAI: {}", doc.getFileUrl());
        try {
            String transcript = assemblyAIService.transcribeVideo(doc.getFileUrl(), "ja");
            if (transcript != null && !transcript.isBlank()) {
                doc.setExtractedContent(truncate(transcript, MAX_TEXT_CHARS));
                documentRepository.save(doc);
                log.info("[PlacementDoc] AssemblyAI transcript cached ({} chars)", transcript.length());
                return transcript;
            }
        } catch (Exception e) {
            log.warn("[PlacementDoc] AssemblyAI transcription failed: {}", e.getMessage());
        }
        return buildFallbackContent(doc);
    }

    private String buildReadingContentForAI(PlacementDocument doc) {
        if (doc.getExtractedContent() != null && !doc.getExtractedContent().isBlank()) {
            return doc.getExtractedContent();
        }
        return buildFallbackContent(doc);
    }

    private String buildFallbackContent(PlacementDocument doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tên tài liệu: ").append(doc.getTitle()).append("\n");
        if (doc.getDescription() != null && !doc.getDescription().isBlank()) {
            sb.append("Mô tả: ").append(doc.getDescription()).append("\n");
        }
        if (doc.getTargetLevel() != null) {
            sb.append("Cấp độ: ").append(doc.getTargetLevel().name()).append("\n");
        }
        return sb.toString();
    }

    private String buildReadingPrompt(PlacementDocument doc, String content, int count) {
        String levelInfo = doc.getTargetLevel() != null ? doc.getTargetLevel().name() : "N3-N4";
        return String.format("""
                Bạn là chuyên gia ra đề thi tiếng Nhật (JLPT).
                Dưới đây là NỘI DUNG THẬT của tài liệu tiếng Nhật:
                
                ===== NỘI DUNG TÀI LIỆU =====
                %s
                ==============================
                
                Hãy sinh %d câu hỏi trắc nghiệm tiếng Nhật DỰA TRÊN NỘI DUNG TRÊN.
                Cấp độ JLPT: %s
                
                YÊU CẦU:
                1. Câu hỏi PHẢI dựa trên nội dung thật trong tài liệu (ngữ pháp, từ vựng, hán tự, đọc hiểu)
                2. Mỗi câu có 4 đáp án A/B/C/D (1 đúng, 3 sai hợp lý)
                3. Đa dạng dạng câu: điền vào chỗ trống, chọn nghĩa đúng, đọc hán tự, ngữ pháp...
                4. Trả về JSON array THUẦN TÚY (không có bất kỳ text nào ngoài JSON)
                
                FORMAT JSON:
                [
                  {
                    "content": "次の（　）に入る言葉はどれですか？「...」",
                    "options": {"A": "...", "B": "...", "C": "...", "D": "..."},
                    "correctAnswer": "A",
                    "explanation": "Giải thích tiếng Việt tại sao đáp án đúng",
                    "topic": "Ngữ pháp / Từ vựng / Hán tự / Đọc hiểu",
                    "jlptLevel": "N5|N4|N3|N2|N1"
                  }
                ]
                """,
                content, count, levelInfo
        );
    }

    private String buildListeningPrompt(PlacementDocument doc, String transcript, int count) {
        String levelInfo = doc.getTargetLevel() != null ? doc.getTargetLevel().name() : "N4-N5";
        boolean hasRealTranscript = !transcript.startsWith("Tên tài liệu:");

        String contentSection = hasRealTranscript
                ? "===== NỘI DUNG TRANSCRIPT THẬT CỦA FILE NGHE =====\n" + transcript + "\n======================================================"
                : "===== THÔNG TIN FILE NGHE =====\n" + transcript + "\n================================";

        return String.format("""
                Bạn là chuyên gia ra đề thi NGHE tiếng Nhật (JLPT Listening).
                %s
                
                Hãy sinh %d câu hỏi LISTENING dựa trên nội dung trên.
                Cấp độ: %s
                
                HƯỚNG DẪN:
                - "content": Viết câu dẫn ngắn gọn VÀ câu hỏi cụ thể về nội dung nghe
                  Ví dụ: "Nghe đoạn hội thoại và chọn đáp án đúng: Người phụ nữ muốn làm gì?"
                - Đáp án phải liên quan đến nội dung transcript
                - 1 đúng, 3 sai hợp lý (không quá dễ đoán)
                - Trả về JSON array THUẦN TÚY
                
                FORMAT JSON:
                [
                  {
                    "content": "Nghe đoạn hội thoại và trả lời: [câu hỏi cụ thể]",
                    "options": {"A": "...", "B": "...", "C": "...", "D": "..."},
                    "correctAnswer": "B",
                    "explanation": "Giải thích tiếng Việt (trích dẫn phần nào trong audio)",
                    "topic": "Nghe hiểu",
                    "jlptLevel": "N5|N4|N3|N2|N1"
                  }
                ]
                """,
                contentSection, count, levelInfo
        );
    }

    private String buildMixedPrompt(List<PlacementDocument> docs, String combinedContent, int totalQuestions) {
        String docTitles = docs.stream()
                .map(d -> "\"" + d.getTitle() + "\"")
                .collect(Collectors.joining(", "));

        return String.format("""
                Bạn là chuyên gia ra đề thi tiếng Nhật (JLPT).
                Dưới đây là NỘI DUNG THẬT được trích từ %d tài liệu: %s
                
                ===== NỘI DUNG CÁC TÀI LIỆU =====
                %s
                ===================================
                
                Hãy sinh %d câu hỏi trắc nghiệm TRỘN từ nhiều tài liệu trên.
                
                YÊU CẦU:
                1. Mỗi câu dựa trên nội dung thực từ ít nhất 1 tài liệu
                2. Cân bằng level: mỗi level N5/N4/N3/N2 khoảng %d câu
                3. Đa dạng: ngữ pháp, từ vựng, hán tự, đọc hiểu
                4. Mỗi câu có 4 đáp án A/B/C/D
                5. Trả về JSON array THUẦN TÚY
                
                FORMAT JSON:
                [
                  {
                    "content": "...",
                    "options": {"A": "...", "B": "...", "C": "...", "D": "..."},
                    "correctAnswer": "A",
                    "explanation": "Giải thích tiếng Việt",
                    "topic": "...",
                    "jlptLevel": "N5|N4|N3|N2|N1"
                  }
                ]
                """,
                docs.size(), docTitles, combinedContent, totalQuestions, Math.max(1, totalQuestions / 4)
        );
    }

    private List<PlacementQuestion> parseGeneratedQuestions(String rawJson, PlacementDocument sourceDoc) {
        List<PlacementQuestion> result = new ArrayList<>();
        try {
            String cleanJson = rawJson.replace("```json", "").replace("```", "").trim();
            JSONArray arr = new JSONArray(cleanJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String content = obj.optString("content", "");
                if (content.isBlank()) continue;

                Map<String, String> options = new LinkedHashMap<>();
                JSONObject optObj = obj.optJSONObject("options");
                if (optObj != null) {
                    for (String key : List.of("A", "B", "C", "D")) {
                        options.put(key, optObj.optString(key, ""));
                    }
                }

                result.add(PlacementQuestion.builder()
                        .content(content)
                        .options(options)
                        .correctAnswer(obj.optString("correctAnswer", "A"))
                        .explanation(obj.optString("explanation", ""))
                        .topic(obj.optString("topic", "Tổng hợp"))
                        .jlptLevel(parseJlptLevel(obj.optString("jlptLevel", ""), sourceDoc.getTargetLevel()))
                        .source(sourceDoc.getTitle())
                        .questionType(QuestionType.READING)
                        .sourceDocumentId(sourceDoc.getId())
                        .aiGenerated(true)
                        .build());
            }
        } catch (Exception e) {
            log.error("[PlacementDoc] Parse questions failed: {}", e.getMessage());
        }
        return result;
    }

    private List<PlacementQuestion> parseListeningQuestions(String rawJson, PlacementDocument sourceDoc) {
        List<PlacementQuestion> result = parseGeneratedQuestions(rawJson, sourceDoc);
        result.forEach(q -> {
            q.setQuestionType(QuestionType.LISTENING);
            q.setAudioUrl(sourceDoc.getFileUrl());
        });
        return result;
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File r\u1ed7ng ho\u1eb7c kh\u00f4ng c\u00f3 n\u1ed9i dung. Vui l\u00f2ng ch\u1ecdn file h\u1ee3p l\u1ec7.");
        if (file.getSize() > MAX_DOCUMENT_SIZE)
            throw new IllegalArgumentException("File qu\u00e1 l\u1edbn. T\u1ed1i \u0111a 20MB cho PDF/DOC (hi\u1ec7n t\u1ea1i: "
                    + String.format("%.1f", file.getSize() / 1024.0 / 1024.0) + "MB)");
        String ct = file.getContentType();
        String fn = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        boolean validType = ct != null && ALLOWED_DOCUMENT_TYPES.contains(ct);
        boolean validExt  = fn.endsWith(".pdf") || fn.endsWith(".doc") || fn.endsWith(".docx");
        if (!validType && !validExt) throw new AppException(ErrorCode.DOCUMENT_NOT_PDF);
    }

    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File \u00e2m thanh r\u1ed7ng ho\u1eb7c kh\u00f4ng c\u00f3 n\u1ed9i dung.");
        if (file.getSize() > MAX_AUDIO_SIZE)
            throw new IllegalArgumentException("File \u00e2m thanh qu\u00e1 l\u1edbn. T\u1ed1i \u0111a 50MB (hi\u1ec7n t\u1ea1i: "
                    + String.format("%.1f", file.getSize() / 1024.0 / 1024.0) + "MB)");
        String ct = file.getContentType();
        String fn = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        boolean validType = ct != null && ALLOWED_AUDIO_TYPES.contains(ct);
        boolean validExt  = fn.endsWith(".mp3") || fn.endsWith(".wav") || fn.endsWith(".m4a") || fn.endsWith(".ogg");
        if (!validType && !validExt) throw new AppException(ErrorCode.AUDIO_INVALID);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "unknown";
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max) + "... [truncated]" : text;
    }

    private JlptLevel parseLevel(String s) {
        try { return s != null ? JlptLevel.valueOf(s.toUpperCase().trim()) : null; } catch (Exception e) { return null; }
    }

    private JlptLevel parseJlptLevel(String s, JlptLevel fallback) {
        try { return JlptLevel.valueOf(s.toUpperCase().trim()); } catch (Exception e) { return fallback != null ? fallback : JlptLevel.N4; }
    }

    private User getCurrentUser() {
        try {
            return userRepository.findByEmail(
                    SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
        } catch (Exception e) { return null; }
    }

    private GenerateQuestionsResponse buildResponse(PlacementDocument doc,
                                                     List<PlacementQuestion> questions,
                                                     String msgTemplate) {
        return GenerateQuestionsResponse.builder()
                .documentId(doc.getId())
                .documentTitle(doc.getTitle())
                .questionsGenerated(questions.size())
                .generatedQuestionIds(questions.stream().map(q -> q.getId().toString()).collect(Collectors.toList()))
                .message(String.format(msgTemplate, questions.size(), doc.getTitle()))
                .build();
    }

    private PlacementDocumentResponse toResponse(PlacementDocument doc) {
        return PlacementDocumentResponse.builder()
                .documentId(doc.getId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .fileUrl(doc.getFileUrl())
                .fileType(doc.getFileType())
                .documentType(doc.getDocumentType())
                .targetLevel(doc.getTargetLevel())
                .status(doc.getStatus())
                .generatedQuestionCount(doc.getGeneratedQuestionCount())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .uploadedByName(doc.getUploadedBy() != null ? doc.getUploadedBy().getFullName() : null)
                .build();
    }
}
