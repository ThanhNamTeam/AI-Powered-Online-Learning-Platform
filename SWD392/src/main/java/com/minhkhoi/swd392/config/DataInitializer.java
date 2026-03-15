package com.minhkhoi.swd392.config;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.entity.Module;
import com.minhkhoi.swd392.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final DiscussionRepository discussionRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Creating sample data...");
        try {
            // 1. Initializing Users
            if (userRepository.findByEmail("admin@sabo.com").isEmpty()) {
                userRepository.save(User.builder()
                        .fullName("Administrator").email("admin@sabo.com")
                        .passwordHash(passwordEncoder.encode("Admin@123"))
                        .role(User.Role.ADMIN).createdAt(LocalDateTime.now()).enabled(true).build());
            }

            User staff = userRepository.findByEmail("staff@swd392.com").orElseGet(() -> 
                userRepository.save(User.builder()
                        .fullName("Staff User").email("staff@swd392.com")
                        .passwordHash(passwordEncoder.encode("Staff@123"))
                        .role(User.Role.STAFF).createdAt(LocalDateTime.now()).enabled(true).build())
            );

            User instructor = userRepository.findByEmail("instructor@gmail.com").orElseGet(() -> 
                userRepository.save(User.builder()
                        .fullName("Instructor User").email("instructor@gmail.com")
                        .passwordHash(passwordEncoder.encode("Instructor@123"))
                        .role(User.Role.INSTRUCTOR).createdAt(LocalDateTime.now()).enabled(true).build())
            );

            User student = userRepository.findByEmail("student@gmail.com").orElseGet(() -> 
                userRepository.save(User.builder()
                        .fullName("Student User").email("student@gmail.com")
                        .passwordHash(passwordEncoder.encode("Student@123"))
                        .role(User.Role.STUDENT).createdAt(LocalDateTime.now()).enabled(true).build())
            );

            // 2. Sample Course
            Course course;
            List<Course> courses = courseRepository.findAll();
            if (courses.isEmpty()) {
                course = courseRepository.save(Course.builder()
                        .title("Khóa học Tiếng Nhật N3 (Mẫu)")
                        .description("Nội dung học thử nghiệm.")
                        .price(new BigDecimal("500000"))
                        .status(CourseStatus.APPROVED)
                        .constructor(instructor)
                        .jlptLevel(JlptLevel.N3)
                        .createdAt(LocalDateTime.now())
                        .build());
            } else {
                course = courses.get(0);
            }

            // 3. Sample Modules & Lessons
            Lesson lesson;
            if (lessonRepository.count() == 0) {
                Module module = moduleRepository.save(Module.builder()
                        .course(course).title("Chương 1").orderIndex(1).build());
                lesson = lessonRepository.save(Lesson.builder()
                        .module(module).title("Bài học số 1").orderIndex(1)
                        .videoUrl("https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4")
                        .build());
            } else {
                lesson = lessonRepository.findAll().get(0);
            }

            // 4. Sample Reports
            if (reportRepository.count() == 0) {
                reportRepository.save(Report.builder()
                        .reporter(student).reportedCourse(course)
                        .type(Report.ReportType.COURSE_QUALITY)
                        .title("Nội dung không khớp")
                        .description("Video bài 1 bị lỗi âm thanh.")
                        .status(Report.ReportStatus.PENDING).createdAt(LocalDateTime.now()).build());
            }

            // 5. Sample Discussions
            if (discussionRepository.count() == 0) {
                discussionRepository.save(Discussion.builder()
                        .user(student).lesson(lesson)
                        .content("Câu hỏi về ngữ pháp bài 1")
                        .type(Discussion.DiscussionType.QUESTION)
                        .status(Discussion.DiscussionStatus.UNANSWERED)
                        .createdAt(LocalDateTime.now().minusHours(2)).likes(5).build());
                
                discussionRepository.save(Discussion.builder()
                        .user(student).lesson(lesson)
                        .content("Bình luận về chất lượng bài giảng")
                        .type(Discussion.DiscussionType.DISCUSSION)
                        .status(Discussion.DiscussionStatus.UNANSWERED)
                        .createdAt(LocalDateTime.now().minusDays(1)).likes(10).build());
            }

            log.info("✅ Data initialization completed successfully!");
        } catch (Exception e) {
            log.error("❌ Initialization Error: {}", e.getMessage(), e);
        }
    }
}
