package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.PlacementDocument;
import com.minhkhoi.swd392.entity.PlacementDocument.DocumentStatus;
import com.minhkhoi.swd392.entity.PlacementDocument.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlacementDocumentRepository extends JpaRepository<PlacementDocument, UUID> {

    /** Lấy tất cả tài liệu theo loại (READING hoặc LISTENING) */
    List<PlacementDocument> findByDocumentType(DocumentType documentType);

    /** Lấy tài liệu theo trạng thái */
    List<PlacementDocument> findByStatus(DocumentStatus status);

    /** Lấy tài liệu theo loại và trạng thái */
    List<PlacementDocument> findByDocumentTypeAndStatus(DocumentType documentType, DocumentStatus status);

    /** Đếm số tài liệu theo loại */
    long countByDocumentType(DocumentType documentType);

    /** Tìm các tài liệu READING đã được xử lý để AI tổng hợp */
    @Query("SELECT d FROM PlacementDocument d WHERE d.documentType = 'READING' AND d.status = 'PROCESSED' ORDER BY d.createdAt DESC")
    List<PlacementDocument> findProcessedReadingDocuments();

    /** Tìm các tài liệu LISTENING đã được xử lý */
    @Query("SELECT d FROM PlacementDocument d WHERE d.documentType = 'LISTENING' AND d.status = 'PROCESSED' ORDER BY d.createdAt DESC")
    List<PlacementDocument> findProcessedListeningDocuments();

    /** Lấy ngẫu nhiên N tài liệu PROCESSED theo loại để AI trộn */
    @Query(value = "SELECT * FROM placement_documents WHERE document_type = :type AND status = 'PROCESSED' ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PlacementDocument> findRandomDocumentsByType(@Param("type") String type, @Param("limit") int limit);
}
