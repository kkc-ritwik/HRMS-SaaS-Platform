package com.hrms.recruitment.repository;

import com.hrms.recruitment.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CandidateRepository extends JpaRepository<Candidate, UUID> {

    Optional<Candidate> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Optional<Candidate> findByTenantIdAndEmailAndDeletedFalse(String tenantId, String email);

    boolean existsByTenantIdAndEmailAndDeletedFalse(String tenantId, String email);

    Page<Candidate> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    /** Full-text search on name + email. */
    @Query("SELECT c FROM Candidate c WHERE c.tenantId = :tenantId AND c.deleted = false " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           " OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%',:q,'%')) " +
           " OR LOWER(c.email)     LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Candidate> search(@Param("tenantId") String tenantId,
                           @Param("q") String query,
                           Pageable pageable);

    /** Source breakdown for analytics. */
    @Query("SELECT c.source, COUNT(c) FROM Candidate c " +
           "WHERE c.tenantId = :tenantId AND c.deleted = false GROUP BY c.source")
    java.util.List<Object[]> countBySource(@Param("tenantId") String tenantId);
}
