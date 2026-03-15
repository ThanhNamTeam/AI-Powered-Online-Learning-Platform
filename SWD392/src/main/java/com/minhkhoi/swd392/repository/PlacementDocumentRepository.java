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

    List<PlacementDocument> findByDocumentType(DocumentType documentType);
    @Query(value = "SELECT * FROM placement_documents WHERE document_type = :type AND status = 'PROCESSED' ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PlacementDocument> findRandomDocumentsByType(@Param("type") String type, @Param("limit") int limit);
}
