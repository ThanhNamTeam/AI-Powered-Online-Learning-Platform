package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.status = :status AND (r.reportId = :id OR LOWER(r.reporter.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Report> findByStatusAndSearch(Report.ReportStatus status, UUID id, String search, Pageable pageable);
    
    @Query("SELECT r FROM Report r WHERE r.status = :status AND LOWER(r.reporter.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Report> findByStatusAndSearchByReporter(Report.ReportStatus status, String search, Pageable pageable);

}
