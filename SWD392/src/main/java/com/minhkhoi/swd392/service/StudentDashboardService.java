package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.response.StudentDashboardResponse;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final QuizResultRepository quizResultRepository;
    private final ReviewRepository reviewRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public StudentDashboardResponse getDashboardData(User currentUser) {
        List<Enrollment> enrollments = enrollmentRepository.findByUser_UserId(currentUser.getUserId());

        int totalLessonsCompleted = 0;
        int totalLessons = 0;
        double totalScore = 0;
        int quizCount = 0;

        for (Enrollment e : enrollments) {
            totalLessonsCompleted += (int) progressRepository.countCompletedByEnrollment(e);
            totalLessons += (int) progressRepository.countActiveLessonsByCourseId(e.getCourse().getCourseId());
        }

        List<QuizResult> quizResults = quizResultRepository.findByUser_UserId(currentUser.getUserId());
        for (QuizResult qr : quizResults) {
            totalScore += qr.getScore();
            quizCount++;
        }

        StudentDashboardResponse.Stats stats = StudentDashboardResponse.Stats.builder()
                .totalLearningTime(calculateTotalLearningTime(currentUser))
                .totalCourses(enrollments.size())
                .completionRate(totalLessons > 0 ? (totalLessonsCompleted * 100 / totalLessons) : 0)
                .averageScore(quizCount > 0 ? (totalScore / quizCount) : 0.0)
                .build();
        StudentDashboardResponse.RecentCourse recentCourse = null;
        Enrollment mostRecentEnrollment = findMostRecentEnrollment(enrollments);
        if (mostRecentEnrollment != null) {
            long completedInCourse = progressRepository.countCompletedByEnrollment(mostRecentEnrollment);
            long totalInCourse = progressRepository.countActiveLessonsByCourseId(mostRecentEnrollment.getCourse().getCourseId());
            
            Progress lastProgress = mostRecentEnrollment.getProgressList().stream()
                    .sorted((p1, p2) -> {
                        if (p1.getUpdatedAt() == null) return 1;
                        if (p2.getUpdatedAt() == null) return -1;
                        return p2.getUpdatedAt().compareTo(p1.getUpdatedAt());
                    })
                    .findFirst().orElse(null);

            recentCourse = StudentDashboardResponse.RecentCourse.builder()
                    .courseId(mostRecentEnrollment.getCourse().getCourseId())
                    .title(mostRecentEnrollment.getCourse().getTitle())
                    .currentLesson(lastProgress != null ? lastProgress.getLesson().getTitle() : "Chưa bắt đầu")
                    .progressPercentage(totalInCourse > 0 ? (int)(completedInCourse * 100 / totalInCourse) : 0)
                    .remainingMinutes(calculateRemainingMinutes(mostRecentEnrollment))
                    .level(mostRecentEnrollment.getCourse().getJlptLevel() != null ? 
                            mostRecentEnrollment.getCourse().getJlptLevel().toString() : "N/A")
                    .build();
        }

        List<StudentDashboardResponse.RecommendedCourse> recommended = courseRepository.findTopTrendingCourses("", PageRequest.of(0, 6))
                .getContent().stream()
                .map(c -> StudentDashboardResponse.RecommendedCourse.builder()
                        .courseId(c.getCourseId())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .thumbnailUrl(c.getThumbnailUrl())
                        .rating(getAverageRating(c.getCourseId()))
                        .constructorName(c.getConstructor().getFullName())
                        .price(c.getPrice() != null ? c.getPrice().longValue() : 0L)
                        .status(c.getStatus().toString())
                        .build())
                .collect(Collectors.toList());

        return StudentDashboardResponse.builder()
                .studentName(currentUser.getFullName() != null ? currentUser.getFullName() : "Học viên")
                .learningStreak(currentUser.getStreak() != null ? currentUser.getStreak() : 0)
                .stats(stats)
                .recentCourse(recentCourse)
                .recommendedCourses(recommended)
                .build();
    }

    private double getAverageRating(java.util.UUID courseId) {
        Double avg = reviewRepository.findAverageRatingByCourseId(courseId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    private String calculateTotalLearningTime(User user) {
        Long totalStudyTimeSecondsValue = progressRepository.sumStudyTimeByUserEmail(user.getEmail());
        long totalStudyTimeSeconds = totalStudyTimeSecondsValue != null ? totalStudyTimeSecondsValue : 0;
        
        if (totalStudyTimeSeconds == 0) return "0h";
        
        double hours = totalStudyTimeSeconds / 3600.0;
        if (hours < 0.1) {
            return "< 0.1h";
        }
        
        double roundedHours = Math.round(hours * 10.0) / 10.0;
        return (roundedHours == (int)roundedHours ? (int)roundedHours : roundedHours) + "h";
    }

    private int calculateRemainingMinutes(Enrollment enrollment) {

        return 15;
    }

    private Enrollment findMostRecentEnrollment(List<Enrollment> enrollments) {
        if (enrollments.isEmpty()) return null;
        
        return enrollments.stream()
                .filter(e -> e.getProgressList() != null && !e.getProgressList().isEmpty())
                .sorted((e1, e2) -> {
                    LocalDateTime t1 = e1.getProgressList().stream()
                            .map(Progress::getUpdatedAt)
                            .filter(java.util.Objects::nonNull)
                            .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                    LocalDateTime t2 = e2.getProgressList().stream()
                            .map(Progress::getUpdatedAt)
                            .filter(java.util.Objects::nonNull)
                            .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
                    return t2.compareTo(t1);
                })
                .findFirst()
                .orElse(enrollments.get(0));
    }
}
