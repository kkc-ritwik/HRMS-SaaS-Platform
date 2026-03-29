package com.hrms.workflow.repository;

import com.hrms.workflow.entity.WorkflowDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {

    Optional<WorkflowDefinition> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<WorkflowDefinition> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<WorkflowDefinition> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<WorkflowDefinition> findByTenantIdAndEntityTypeAndActiveAndDeletedFalse(String tenantId, String entityType, boolean active);

    Optional<WorkflowDefinition> findByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);
}
