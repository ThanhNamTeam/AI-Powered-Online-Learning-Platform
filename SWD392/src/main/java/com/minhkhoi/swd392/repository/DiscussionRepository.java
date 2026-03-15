package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, UUID> {
    
    Page<Discussion> findByTypeAndStatus(Discussion.DiscussionType type, Discussion.DiscussionStatus status, Pageable pageable);

    Page<Discussion> findByStatus(Discussion.DiscussionStatus status, Pageable pageable);
    
    Page<Discussion> findByType(Discussion.DiscussionType type, Pageable pageable);

    @Query("SELECT d FROM Discussion d WHERE " +
           "(:type IS NULL OR d.type = :type) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(d.content) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Discussion> findWithFilters(@Param("type") Discussion.DiscussionType type, @Param("status") Discussion.DiscussionStatus status, @Param("search") String search, Pageable pageable);


    long countByStatus(Discussion.DiscussionStatus status);
}
