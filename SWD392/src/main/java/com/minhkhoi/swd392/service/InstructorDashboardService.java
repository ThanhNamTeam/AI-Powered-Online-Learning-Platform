package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.response.InstructorDashboardResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Enrollment;
import com.minhkhoi.swd392.entity.Payment;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.PaymentRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorDashboardService {

    private final UserRepository       userRepository;
    private final CourseRepository     courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository    paymentRepository;

    // ── Inner DTOs (shared với controller) ──────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourseItem {
        private String     courseId;
        private String     title;
        private String     description;
        private String     jlptLevel;
        private String     status;
        private BigDecimal price;
        private String     thumbnailUrl;
        private String     createdAt;
        private long       enrollmentCount;
        private String     rejectionReason;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StudentItem {
        private String       userId;
        private String       fullName;
        private String       email;
        private String       imageUrl;
        private int          enrolledCourseCount;
        private List<String> enrolledCourseTitles;
        private String       lastEnrolledAt;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private LocalDateTime startOf(LocalDateTime base, int monthsAgo) {
        return base.minusMonths(monthsAgo)
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private BigDecimal sumAmounts(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String fmtMonth(LocalDateTime dt) {
        return "T" + dt.getMonthValue() + "/" + String.valueOf(dt.getYear()).substring(2);
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── getMyCourses ─────────────────────────────────────────────────────────

    /**
     * Trả về danh sách khóa học của GV đang login, tuỳ chọn filter theo status.
     * @Transactional bắt buộc vì cần access lazy collection enrollment count.
     */
    @Transactional(readOnly = true)
    public List<CourseItem> getMyCourses(String status) {
        String email = currentEmail();
        List<Course> courses = courseRepository.findByConstructor_Email(email);

        // Map courseId → enrollment count
        List<UUID> courseIds = courses.stream()
                .map(Course::getCourseId).collect(Collectors.toList());

        Map<UUID, Long> enrollCount = enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse() != null && courseIds.contains(e.getCourse().getCourseId()))
                .collect(Collectors.groupingBy(
                        e -> e.getCourse().getCourseId(), Collectors.counting()));

        return courses.stream()
                .filter(c -> status == null || status.isBlank()
                        || (c.getStatus() != null && c.getStatus().name().equals(status)))
                .sorted(Comparator.comparing(
                        c -> c.getCreatedAt() == null ? LocalDateTime.MIN : c.getCreatedAt(),
                        Comparator.reverseOrder()))
                .map(c -> CourseItem.builder()
                        .courseId(c.getCourseId().toString())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .jlptLevel(c.getJlptLevel() != null ? c.getJlptLevel().name() : null)
                        .status(c.getStatus() != null ? c.getStatus().name() : null)
                        .price(c.getPrice())
                        .thumbnailUrl(c.getThumbnailUrl())
                        .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FMT) : null)
                        .enrollmentCount(enrollCount.getOrDefault(c.getCourseId(), 0L))
                        .rejectionReason(c.getRejectionReason())
                        .build())
                .collect(Collectors.toList());
    }

    // ── getMyStudents ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StudentItem> getMyStudents() {
        String email = currentEmail();
        List<Course> myCourses = courseRepository.findByConstructor_Email(email);
        Set<UUID> myCourseIds = myCourses.stream()
                .map(Course::getCourseId).collect(Collectors.toSet());

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse() != null
                        && myCourseIds.contains(e.getCourse().getCourseId())
                        && e.getUser() != null)
                .collect(Collectors.toList());

        // Group by userId
        Map<String, List<Enrollment>> byStudent = enrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getUser().getUserId()));

        return byStudent.values().stream()
                .map(list -> {
                    User u = list.get(0).getUser();
                    List<String> titles = list.stream()
                            .filter(e -> e.getCourse() != null)
                            .map(e -> e.getCourse().getTitle())
                            .collect(Collectors.toList());
                    Enrollment latest = list.stream()
                            .max(Comparator.comparing(
                                    e -> e.getEnrolledAt() == null ? LocalDateTime.MIN : e.getEnrolledAt()))
                            .orElse(null);
                    return StudentItem.builder()
                            .userId(u.getUserId())
                            .fullName(u.getFullName())
                            .email(u.getEmail())
                            .imageUrl(u.getImageUrl())
                            .enrolledCourseCount(list.size())
                            .enrolledCourseTitles(titles)
                            .lastEnrolledAt(latest != null && latest.getEnrolledAt() != null
                                    ? latest.getEnrolledAt().format(DATE_FMT) : null)
                            .build();
                })
                .sorted(Comparator.comparing(s -> s.getFullName() != null ? s.getFullName() : ""))
                .collect(Collectors.toList());
    }

    // ── getDashboard ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InstructorDashboardResponse getDashboard() {

        String email = currentEmail();
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Instructor not found: " + email));

        List<Course> myCourses = courseRepository.findByConstructor_Email(email);
        Set<UUID> myCourseIds  = myCourses.stream()
                .map(Course::getCourseId).collect(Collectors.toSet());

        List<Enrollment> allEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse() != null && myCourseIds.contains(e.getCourse().getCourseId()))
                .collect(Collectors.toList());

        List<Payment> allPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED
                        && p.getEnrollment() != null
                        && p.getEnrollment().getCourse() != null
                        && myCourseIds.contains(p.getEnrollment().getCourse().getCourseId()))
                .collect(Collectors.toList());

        // Stats
        long totalCourses = myCourses.size();
        long approved = myCourses.stream().filter(c -> c.getStatus() == CourseStatus.APPROVED).count();
        long pending  = myCourses.stream().filter(c -> c.getStatus() == CourseStatus.PENDING_APPROVAL).count();
        long draft    = myCourses.stream().filter(c -> c.getStatus() == CourseStatus.DRAFT).count();
        long rejected = myCourses.stream().filter(c -> c.getStatus() == CourseStatus.REJECTED).count();

        LocalDateTime now            = LocalDateTime.now();
        LocalDateTime thisMonthStart = startOf(now, 0);
        LocalDateTime lastMonthStart = startOf(now, 1);

        BigDecimal totalRevenue = sumAmounts(allPayments);
        BigDecimal currentMonthRevenue = sumAmounts(allPayments.stream()
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(thisMonthStart))
                .collect(Collectors.toList()));
        BigDecimal lastMonthRevenue = sumAmounts(allPayments.stream()
                .filter(p -> p.getCreatedAt() != null
                        && !p.getCreatedAt().isBefore(lastMonthStart)
                        && p.getCreatedAt().isBefore(thisMonthStart))
                .collect(Collectors.toList()));

        // Charts
        List<InstructorDashboardResponse.MonthlyRevenueStat> revenueByMonth =
                buildMonthlyRevenue(allPayments, now);
        List<InstructorDashboardResponse.MonthlyEnrollmentStat> enrollmentByMonth =
                buildMonthlyEnrollments(allEnrollments, now);

        // Top 5 courses
        Map<UUID, Long> enrollCountMap = allEnrollments.stream()
                .filter(e -> e.getCourse() != null)
                .collect(Collectors.groupingBy(e -> e.getCourse().getCourseId(), Collectors.counting()));
        Map<UUID, BigDecimal> revenueMap = new HashMap<>();
        for (Payment p : allPayments) {
            if (p.getEnrollment() != null && p.getEnrollment().getCourse() != null && p.getAmount() != null) {
                revenueMap.merge(p.getEnrollment().getCourse().getCourseId(), p.getAmount(), BigDecimal::add);
            }
        }

        List<InstructorDashboardResponse.CoursePerformance> topCourses = myCourses.stream()
                .map(c -> InstructorDashboardResponse.CoursePerformance.builder()
                        .courseId(c.getCourseId().toString())
                        .title(c.getTitle())
                        .jlptLevel(c.getJlptLevel() != null ? c.getJlptLevel().name() : null)
                        .enrollmentCount(enrollCountMap.getOrDefault(c.getCourseId(), 0L))
                        .revenue(revenueMap.getOrDefault(c.getCourseId(), BigDecimal.ZERO))
                        .status(c.getStatus() != null ? c.getStatus().name() : null)
                        .build())
                .sorted(Comparator.comparingLong(
                        InstructorDashboardResponse.CoursePerformance::getEnrollmentCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Course info list
        List<InstructorDashboardResponse.MyCourseInfo> courseInfoList = myCourses.stream()
                .sorted(Comparator.comparing(
                        c -> c.getCreatedAt() == null ? LocalDateTime.MIN : c.getCreatedAt(),
                        Comparator.reverseOrder()))
                .map(c -> InstructorDashboardResponse.MyCourseInfo.builder()
                        .courseId(c.getCourseId().toString())
                        .title(c.getTitle())
                        .jlptLevel(c.getJlptLevel() != null ? c.getJlptLevel().name() : null)
                        .status(c.getStatus() != null ? c.getStatus().name() : null)
                        .price(c.getPrice())
                        .thumbnailUrl(c.getThumbnailUrl())
                        .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FMT) : "N/A")
                        .enrollmentCount(enrollCountMap.getOrDefault(c.getCourseId(), 0L))
                        .rejectionReason(c.getRejectionReason())
                        .build())
                .collect(Collectors.toList());

        return InstructorDashboardResponse.builder()
                .totalCourses(totalCourses)
                .approvedCourses(approved)
                .pendingCourses(pending)
                .draftCourses(draft)
                .rejectedCourses(rejected)
                .totalEnrollments(allEnrollments.size())
                .totalRevenue(totalRevenue)
                .currentMonthRevenue(currentMonthRevenue)
                .lastMonthRevenue(lastMonthRevenue)
                .revenueByMonth(revenueByMonth)
                .enrollmentByMonth(enrollmentByMonth)
                .topCourses(topCourses)
                .myCourses(courseInfoList)
                .build();
    }

    // ── Chart builders ───────────────────────────────────────────────────────

    private List<InstructorDashboardResponse.MonthlyRevenueStat> buildMonthlyRevenue(
            List<Payment> payments, LocalDateTime now) {
        List<InstructorDashboardResponse.MonthlyRevenueStat> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime s = startOf(now, i);
            LocalDateTime e = s.plusMonths(1);
            BigDecimal rev  = sumAmounts(payments.stream()
                    .filter(p -> p.getCreatedAt() != null
                            && !p.getCreatedAt().isBefore(s) && p.getCreatedAt().isBefore(e))
                    .collect(Collectors.toList()));
            result.add(InstructorDashboardResponse.MonthlyRevenueStat.builder()
                    .month(fmtMonth(s)).revenue(rev).build());
        }
        return result;
    }

    private List<InstructorDashboardResponse.MonthlyEnrollmentStat> buildMonthlyEnrollments(
            List<Enrollment> enrollments, LocalDateTime now) {
        List<InstructorDashboardResponse.MonthlyEnrollmentStat> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime s = startOf(now, i);
            LocalDateTime e = s.plusMonths(1);
            long count = enrollments.stream()
                    .filter(en -> en.getEnrolledAt() != null
                            && !en.getEnrolledAt().isBefore(s) && en.getEnrolledAt().isBefore(e))
                    .count();
            result.add(InstructorDashboardResponse.MonthlyEnrollmentStat.builder()
                    .month(fmtMonth(s)).count(count).build());
        }
        return result;
    }
}
