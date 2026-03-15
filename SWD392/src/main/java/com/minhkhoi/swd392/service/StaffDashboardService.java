package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.response.StaffDashboardResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Payment;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.PaymentRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PaymentRepository paymentRepository;

    public StaffDashboardResponse getDashboardStats() {
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long newStudentsToday = userRepository.countByRoleAndCreatedAtAfter(User.Role.STUDENT, startOfToday);
        
        BigDecimal revenueToday = paymentRepository.sumCompletedAmountAfter(startOfToday);
        if (revenueToday == null) revenueToday = BigDecimal.ZERO;

        long pendingRequests = courseRepository.countByStatus(CourseStatus.PENDING_APPROVAL);

        List<StaffDashboardResponse.WeeklyStat> weeklyPerformance = buildWeeklyPerformance();
        List<StaffDashboardResponse.TopCourseInfo> topCourses = buildTopCourses();

        return StaffDashboardResponse.builder()
                .newStudentsToday(newStudentsToday)
                .revenueToday(revenueToday)
                .pendingRequests(pendingRequests)
                .averageRating(4.8)
                .weeklyPerformance(weeklyPerformance)
                .topTrendingCourses(topCourses)
                .build();
    }

    private List<StaffDashboardResponse.WeeklyStat> buildWeeklyPerformance() {
        List<StaffDashboardResponse.WeeklyStat> stats = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

            long registrations = userRepository.countByRoleAndCreatedAtAfter(User.Role.STUDENT, startOfDay);
            
            BigDecimal revenue = paymentRepository.findByStatusAndCreatedAtAfter(Payment.PaymentStatus.COMPLETED, startOfDay)
                    .stream()
                    .filter(p -> p.getCreatedAt().isBefore(endOfDay))
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String dayLabel = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi", "VN"));
            
            stats.add(StaffDashboardResponse.WeeklyStat.builder()
                    .day(dayLabel)
                    .registrations(registrations)
                    .revenue(revenue)
                    .build());
        }
        return stats;
    }

    private List<StaffDashboardResponse.TopCourseInfo> buildTopCourses() {
        return courseRepository.findTopTrendingCourses("", PageRequest.of(0, 5))
                .getContent()
                .stream()
                .map(c -> StaffDashboardResponse.TopCourseInfo.builder()
                        .courseId(c.getCourseId().toString())
                        .title(c.getTitle())
                        .code(c.getCourseId().toString().substring(0, 5).toUpperCase())
                        .students(c.getEnrollments() != null ? c.getEnrollments().size() : 0)
                        .rating(4.5 + (Math.random() * 0.5))
                        .build())
                .collect(Collectors.toList());
    }
}
