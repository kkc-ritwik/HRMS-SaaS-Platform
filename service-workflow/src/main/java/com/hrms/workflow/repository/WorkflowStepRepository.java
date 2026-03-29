package com.hrms.workflow.repository;

import com.hrms.workflow.entity.WorkflowStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {

    Optional<WorkflowStep> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<WorkflowStep> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<WorkflowStep> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<WorkflowStep> findByTenantIdAndWorkflowIdAndDeletedFalseOrderByStepOrderAsc(String tenantId, UUID workflowId);
}
