package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.entity.Module;
import com.minhkhoi.swd392.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final GeminiService geminiService;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    public String chat(String email, String question, UUID lessonId) {
        User user = userRepository.findByEmail(email).orElse(null);
        String studentName = user != null ? user.getFullName() : "bạn";

        String context = buildRagContext(email, lessonId, user);
        String prompt = buildPrompt(question, context, studentName);

        try {
            return geminiService.callGeminiWithPrompt(prompt);
        } catch (Exception e) {
            log.error("Gemini error: {}", e.getMessage());
            return "Xin lỗi, Sensei AI đang gặp sự cố. Vui lòng thử lại sau nhé! 🙏";
        }
    }

    private String buildRagContext(String email, UUID priorityLessonId, User user) {
        List<String> contextParts = new ArrayList<>();

        if (priorityLessonId != null) {
            lessonRepository.findById(priorityLessonId).ifPresent(lesson -> {
                String lessonCtx = extractLessonContent(lesson);
                if (!lessonCtx.isBlank()) {
                    contextParts.add("=== BÀI HỌC ĐANG XEM: " + lesson.getTitle() + " ===\n" + lessonCtx);
                }
            });
        }

        List<Course> allCourses = courseRepository.findByStatus(CourseStatus.APPROVED,
                org.springframework.data.domain.PageRequest.of(0, 50)).getContent();

        if (!allCourses.isEmpty()) {
            StringBuilder catalogue = new StringBuilder();
            catalogue.append("=== CATALOGUE TẤT CẢ KHÓA HỌC TRÊN SABO ACADEMY ===\n");
            catalogue.append("(Tổng cộng ").append(allCourses.size()).append(" khóa học)\n\n");

            for (Course course : allCourses) {
                catalogue.append("📚 KHÓA HỌC: ").append(course.getTitle()).append("\n");
                catalogue.append("   - Cấp độ JLPT: ").append(course.getJlptLevel() != null ? course.getJlptLevel() : "Chưa phân loại").append("\n");
                catalogue.append("   - Giá: ").append(formatPrice(course.getPrice())).append("\n");
                if (course.getDescription() != null && !course.getDescription().isBlank()) {
                    catalogue.append("   - Mô tả: ").append(
                            course.getDescription().substring(0, Math.min(course.getDescription().length(), 200))
                    ).append("\n");
                }
                if (course.getModules() != null && !course.getModules().isEmpty()) {
                    catalogue.append("   - Nội dung gồm: ");
                    List<String> moduleNames = course.getModules().stream()
                            .map(Module::getTitle)
                            .limit(5)
                            .toList();
                    catalogue.append(String.join(", ", moduleNames));
                    if (course.getModules().size() > 5) {
                        catalogue.append(" và ").append(course.getModules().size() - 5).append(" phần nữa");
                    }
                    catalogue.append("\n");
                }
                catalogue.append("\n");
            }
            contextParts.add(catalogue.toString());
        }

        if (user != null) {
            List<Enrollment> enrollments = enrollmentRepository.findByUser_UserId(user.getUserId());
            StringBuilder enrolledContent = new StringBuilder();
            boolean hasContent = false;

            for (Enrollment enrollment : enrollments) {
                String courseTitle = enrollment.getCourse().getTitle();
                if (enrollment.getCourse().getModules() != null) {
                    for (Module module : enrollment.getCourse().getModules()) {
                        if (module.getLessons() == null) continue;
                        for (Lesson lesson : module.getLessons()) {
                            if (priorityLessonId != null && lesson.getLessonId().equals(priorityLessonId)) continue;
                            String content = extractLessonContent(lesson);
                            if (!content.isBlank()) {
                                if (!hasContent) {
                                    enrolledContent.append("=== NỘI DUNG CHI TIẾT KHÓA HỌC ĐÃ ĐĂNG KÝ ===\n");
                                    hasContent = true;
                                }
                                enrolledContent.append("Khóa: ").append(courseTitle)
                                        .append(" | Bài: ").append(lesson.getTitle()).append("\n");
                                enrolledContent.append(content, 0, Math.min(content.length(), 400)).append("\n\n");
                            }
                        }
                    }
                }
            }
            if (hasContent) {
                contextParts.add(enrolledContent.toString());
            }
        }

        if (contextParts.isEmpty()) {
            return "Hiện tại chưa có nội dung khóa học trong hệ thống.";
        }

        String fullContext = String.join("\n\n", contextParts);
        if (fullContext.length() > 10000) {
            fullContext = fullContext.substring(0, 10000) + "\n... [nội dung bị rút gọn]";
        }

        return fullContext;
    }

    private String extractLessonContent(Lesson lesson) {
        StringBuilder sb = new StringBuilder();
        if (lesson.getTranscript() != null && !lesson.getTranscript().isBlank()) {
            sb.append("[Transcript]: ").append(lesson.getTranscript(), 0,
                    Math.min(lesson.getTranscript().length(), 800));
        }
        if (lesson.getDocumentContent() != null && !lesson.getDocumentContent().isBlank()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("[Tài liệu]: ").append(lesson.getDocumentContent(), 0,
                    Math.min(lesson.getDocumentContent().length(), 400));
        }
        return sb.toString();
    }

    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) return "Miễn phí";
        if (price.compareTo(java.math.BigDecimal.ZERO) == 0) return "Miễn phí";
        return String.format("%,.0f VNĐ", price);
    }

    private String buildPrompt(String question, String context, String studentName) {
        return """
                Bạn là **Sensei AI** — trợ lý học tiếng Nhật thông minh của nền tảng SABO Academy.
                Nhiệm vụ của bạn là trả lời câu hỏi của học viên dựa trên thông tin được cung cấp.
                
                HƯỚNG DẪN:
                - Trả lời bằng tiếng Việt, rõ ràng, thân thiện.
                - Dùng thông tin trong Context bên dưới để trả lời chính xác (đặc biệt giá tiền, nội dung khóa học).
                - Nếu hỏi về khóa học → dùng dữ liệu từ CATALOGUE để trả lời đầy đủ.
                - Nếu câu hỏi về tiếng Nhật/văn hóa Nhật nhưng không có trong context → trả lời từ kiến thức chung.
                - Nếu câu hỏi không liên quan tiếng Nhật → lịch sự từ chối.
                - Dùng emoji và **bold** để câu trả lời sinh động.
                - Tên học viên: %s
                
                === DATA TỪ HỆ THỐNG ===
                %s
                ========================
                
                Câu hỏi của %s: %s
                
                Câu trả lời:
                """.formatted(studentName, context, studentName, question);
    }
}
