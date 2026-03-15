package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.PageResponse;
import com.minhkhoi.swd392.dto.response.ReportResponse;
import com.minhkhoi.swd392.entity.Report;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.ReportRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public PageResponse<ReportResponse> getReports(String statusStr, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Report.ReportStatus status = Report.ReportStatus.PENDING;
        if (statusStr != null) {
            try {
                status = Report.ReportStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        Page<Report> reports;
        if (search != null && !search.trim().isEmpty()) {
            try {
                UUID searchId = UUID.fromString(search);
                reports = reportRepository.findByStatusAndSearch(status, searchId, search, pageable);
            } catch (IllegalArgumentException e) {
                reports = reportRepository.findByStatusAndSearchByReporter(status, search, pageable);
            }
        } else {
            reports = reportRepository.findByStatus(status, pageable);
        }

        return PageResponse.<ReportResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(reports.getTotalPages())
                .totalElements(reports.getTotalElements())
                .data(reports.getContent().stream().map(ReportResponse::fromEntity).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public ReportResponse processReport(UUID reportId, String action, String adminReply) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email).orElse(null);

        report.setHandledByAdmin(admin);
        report.setResolvedAt(LocalDateTime.now());
        if (adminReply != null && !adminReply.trim().isEmpty()) {
            report.setAdminResponse(adminReply);
        }

        switch (action.toUpperCase()) {
            case "IGNORE":
                report.setStatus(Report.ReportStatus.REJECTED);
                break;
            case "WARN":
                report.setStatus(Report.ReportStatus.RESOLVED);
                break;
            case "DELETE":
                report.setStatus(Report.ReportStatus.RESOLVED);
                break;
            default:
                report.setStatus(Report.ReportStatus.RESOLVED);
                break;
        }

        return ReportResponse.fromEntity(reportRepository.save(report));
    }
}
