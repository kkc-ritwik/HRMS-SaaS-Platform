package com.hrms.offboarding.repository;

import com.hrms.offboarding.entity.ExitChecklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExitChecklistRepository extends JpaRepository<ExitChecklist, UUID> {

    Optional<ExitChecklist> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ExitChecklist> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ExitChecklist> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<ExitChecklist> findByTenantIdAndSeparationIdAndDeletedFalse(String tenantId, UUID separationId);

    List<ExitChecklist> findByTenantIdAndAssignedToAndDeletedFalse(String tenantId, UUID assignedTo);
}
