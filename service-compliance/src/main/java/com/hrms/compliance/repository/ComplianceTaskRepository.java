package com.hrms.compliance.repository;

import com.hrms.compliance.entity.ComplianceTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplianceTaskRepository extends JpaRepository<ComplianceTask, UUID> {

    Optional<ComplianceTask> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ComplianceTask> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ComplianceTask> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<ComplianceTask> findByTenantIdAndComplianceItemIdAndDeletedFalse(String tenantId, UUID complianceItemId);

    Page<ComplianceTask> findByTenantIdAndAssigneeIdAndDeletedFalse(String tenantId, UUID assigneeId, Pageable pageable);
}
