package com.hrms.offboarding.repository;

import com.hrms.offboarding.entity.ExitInterview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExitInterviewRepository extends JpaRepository<ExitInterview, UUID> {

    Optional<ExitInterview> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ExitInterview> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ExitInterview> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<ExitInterview> findByTenantIdAndSeparationIdAndDeletedFalse(String tenantId, UUID separationId);
}
