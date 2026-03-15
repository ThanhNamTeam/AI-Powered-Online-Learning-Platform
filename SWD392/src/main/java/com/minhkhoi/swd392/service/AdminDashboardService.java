package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.response.AdminDashboardResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Payment;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.PaymentRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository       userRepository;
    private final CourseRepository     courseRepository;
    private final PaymentRepository    paymentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {

        List<User>    allUsers    = userRepository.findAll();
        List<Course>  allCourses  = courseRepository.findAll();
        List<Payment> allPayments = paymentRepository.findAll();

        long totalStudents    = allUsers.stream().filter(u -> u.getRole() == User.Role.STUDENT).count();
        long totalInstructors = allUsers.stream().filter(u -> u.getRole() == User.Role.INSTRUCTOR).count();

        long totalApproved = allCourses.stream().filter(c -> c.getStatus() == CourseStatus.APPROVED).count();
        long totalPending  = allCourses.stream().filter(c -> c.getStatus() == CourseStatus.PENDING_APPROVAL).count();
        long totalRejected = allCourses.stream().filter(c -> c.getStatus() == CourseStatus.REJECTED).count();

        BigDecimal totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalEnrollments = enrollmentRepository.count();

        List<AdminDashboardResponse.MonthlyUserStat> userGrowth =
                buildUserGrowth(allUsers);

        List<AdminDashboardResponse.MonthlyRevenueStat> revenueGrowth =
                buildRevenueGrowth(allPayments);

        List<AdminDashboardResponse.CourseLevelStat> courseByLevel =
                buildCourseLevelStats(allCourses);

        List<AdminDashboardResponse.PendingCourseInfo> pendingCourses = allCourses.stream()
                .filter(c -> c.getStatus() == CourseStatus.PENDING_APPROVAL)
                .sorted(Comparator.comparing(
                        c -> c.getCreatedAt() == null ? LocalDateTime.MIN : c.getCreatedAt(),
                        Comparator.reverseOrder()))
                .map(c -> AdminDashboardResponse.PendingCourseInfo.builder()
                        .courseId(c.getCourseId().toString())
                        .title(c.getTitle())
                        .instructorName(c.getConstructor() != null ? c.getConstructor().getFullName() : "N/A")
                        .jlptLevel(c.getJlptLevel() != null ? c.getJlptLevel().name() : null)
                        .price(c.getPrice())
                        .createdAt(c.getCreatedAt() != null
                                ? c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                : "N/A")
                        .build())
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalUsers(allUsers.size())
                .totalStudents(totalStudents)
                .totalInstructors(totalInstructors)
                .totalCourses(allCourses.size())
                .totalApprovedCourses(totalApproved)
                .totalPendingCourses(totalPending)
                .totalRejectedCourses(totalRejected)
                .totalRevenue(totalRevenue)
                .totalEnrollments(totalEnrollments)
                .userGrowth(userGrowth)
                .revenueGrowth(revenueGrowth)
                .courseByLevel(courseByLevel)
                .pendingCourses(pendingCourses)
                .build();
    }

    private List<AdminDashboardResponse.MonthlyUserStat> buildUserGrowth(List<User> users) {
        LocalDateTime now = LocalDateTime.now();
        List<AdminDashboardResponse.MonthlyUserStat> result = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd   = monthStart.plusMonths(1);
            String label = formatMonthLabel(monthStart);

            long students = users.stream()
                    .filter(u -> u.getRole() == User.Role.STUDENT
                            && u.getCreatedAt() != null
                            && !u.getCreatedAt().isBefore(monthStart)
                            && u.getCreatedAt().isBefore(monthEnd))
                    .count();

            long instructors = users.stream()
                    .filter(u -> u.getRole() == User.Role.INSTRUCTOR
                            && u.getCreatedAt() != null
                            && !u.getCreatedAt().isBefore(monthStart)
                            && u.getCreatedAt().isBefore(monthEnd))
                    .count();

            result.add(AdminDashboardResponse.MonthlyUserStat.builder()
                    .month(label)
                    .students(students)
                    .instructors(instructors)
                    .build());
        }
        return result;
    }

    private List<AdminDashboardResponse.MonthlyRevenueStat> buildRevenueGrowth(List<Payment> payments) {
        LocalDateTime now = LocalDateTime.now();
        List<AdminDashboardResponse.MonthlyRevenueStat> result = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd   = monthStart.plusMonths(1);
            String label = formatMonthLabel(monthStart);

            BigDecimal revenue = payments.stream()
                    .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED
                            && p.getCreatedAt() != null
                            && !p.getCreatedAt().isBefore(monthStart)
                            && p.getCreatedAt().isBefore(monthEnd))
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(AdminDashboardResponse.MonthlyRevenueStat.builder()
                    .month(label)
                    .revenue(revenue)
                    .build());
        }
        return result;
    }

    private List<AdminDashboardResponse.CourseLevelStat> buildCourseLevelStats(List<Course> courses) {
        Map<String, Long> countMap = courses.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getJlptLevel() != null ? c.getJlptLevel().name() : "NONE",
                        Collectors.counting()
                ));

        List<String> order = List.of("N5", "N4", "N3", "N2", "N1", "NONE");
        return order.stream()
                .filter(countMap::containsKey)
                .map(level -> AdminDashboardResponse.CourseLevelStat.builder()
                        .level(level)
                        .count(countMap.get(level))
                        .build())
                .collect(Collectors.toList());
    }

    private String formatMonthLabel(LocalDateTime dt) {
        return "T" + dt.getMonthValue() + "/" + String.valueOf(dt.getYear()).substring(2);
    }
}

